package com.winforrd.taskflow.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.winforrd.taskflow.common.exception.BusinessException;
import com.winforrd.taskflow.config.AvatarStorageProperties;
import com.winforrd.taskflow.entity.SysUser;
import com.winforrd.taskflow.mapper.SysUserMapper;
import com.winforrd.taskflow.service.UserAvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


/**
 * =========================================================
 * 用户头像服务
 * =========================================================
 * 设计：
 * 文件：
 *     存磁盘
 * 数据库：
 *     只保存访问URL
 * 安全措施：
 * 1. 最大2MB
 * 2. 只接受JPEG / PNG / WEBP
 * 3. 不信任原始文件名
 * 4. UUID重新生成文件名
 * 5. 防目录穿越
 * 6. ImageIO验证图片内容
 */
@Service
@RequiredArgsConstructor
public class UserAvatarServiceImpl implements UserAvatarService {
    /**
     * 2MB。
     */
    private static final long MAX_FILE_SIZE = 2L * 1024L * 1024L;

    /**
     * 允许Content-Type。
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    /**
     * 允许扩展名。
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final SysUserMapper sysUserMapper;

    private final AvatarStorageProperties avatarStorageProperties;

    /**
     * =====================================================
     * 上传头像
     * =====================================================
     */
    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file) {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 校验文件
        validateFile(file);
        // 获取当前用户
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }
        // 获取文件扩展名
        String extension = resolveExtension(file);
        /**
         * 永远不使用客户端原始文件名。
         */
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        // 获取头像目录
        Path avatarDirectory = getAvatarDirectory();
        // 构建目标文件路径
        Path targetPath = avatarDirectory.resolve(newFileName).normalize();
        /**
         * 防御目录穿越。
         */
        if (!targetPath.startsWith(avatarDirectory)) {
            throw new BusinessException("非法文件路径");
        }
        try {
            Files.createDirectories(avatarDirectory);
            /**
             * 写入文件。
             */
            try (
                    // 获取文件输入流
                    InputStream inputStream = file.getInputStream()
            ) {
                // 写入文件
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException exception) {
            throw new BusinessException("头像文件保存失败");
        }
        String avatarUrl = buildAvatarUrl(newFileName);
        /**
         * =================================================
         * 更新数据库
         * =================================================
         * 这里故意使用字符串字段：
         * set("avatar", avatarUrl)
         * 即使你当前SysUser实体没有显式avatar属性，
         * 也不会因为Lambda getter缺失而无法编译。
         */
        int rows = sysUserMapper.update(null, new UpdateWrapper<SysUser>()
                        .eq("id", currentUserId)
                        .set("avatar", avatarUrl));
        if (rows <= 0) {
            /**
             * DB更新失败时，
             * 清理刚才已经写入的文件。
             */
            deleteFileQuietly(targetPath);
            throw new BusinessException("头像信息更新失败");
        }
        /**
         * =================================================
         * 删除旧头像文件
         * =================================================
         * 必须放到DB更新成功以后。
         * 如果旧头像不是本系统本地头像，
         * 不删除。
         */
        String oldAvatar = getAvatarValue(currentUser);
        deleteOldLocalAvatar(oldAvatar, newFileName);
        return avatarUrl;
    }

    /**
     * =====================================================
     * 删除头像
     * =====================================================
     */
    @Override
    @Transactional
    public void removeAvatar() {
        // 获取当前用户ID
        Long currentUserId = StpUtil.getLoginIdAsLong();
        // 获取当前用户
        SysUser currentUser = sysUserMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }
        // 获取旧头像
        String oldAvatar = getAvatarValue(currentUser);
        // 删除旧头像
        sysUserMapper.update(null, new UpdateWrapper<SysUser>().eq("id", currentUserId).set("avatar", null));
        deleteOldLocalAvatar(oldAvatar, null);
    }

    /**
     * =====================================================
     * 文件校验
     * =====================================================
     */
    private void validateFile(MultipartFile file) {
        // 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择头像文件");
        }
        // 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("头像文件不能超过2MB");
        }
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("头像仅支持 JPG、PNG、WEBP 格式");
        }
        // 校验文件扩展名
        String extension = resolveExtension(file);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("头像文件格式不正确");
        }
        /**
         * =================================================
         * 校验真实图片内容
         * =================================================
         * 防止：
         * test.exe
         * 改名：
         * test.jpg
         */
        try (InputStream inputStream = file.getInputStream()) {BufferedImage image = ImageIO.read(inputStream);
            /**
             * 注意：
             * 某些JDK ImageIO默认不支持WEBP。
             * 因此JPEG / PNG进行真实图片解析，
             * WEBP依赖Content-Type + 后缀检查。
             */
            if (!"webp".equals(extension) && image == null) {
                throw new BusinessException("上传文件不是有效图片");
            }
            if (image != null && (image.getWidth() > 4096 || image.getHeight() > 4096)) {
                throw new BusinessException("头像图片尺寸不能超过4096×4096");
            }
        }
        catch (BusinessException exception) {
            throw exception;
        }
        catch (IOException exception) {
            throw new BusinessException("头像文件读取失败");
        }
    }

    /**
     * =====================================================
     * 获取后缀
     * =====================================================
     */
    private String resolveExtension(MultipartFile file) {
        // 获取文件原始名
        String originalFilename = file.getOriginalFilename();
        // 校验文件原始名是否有效
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException("头像文件名无效");
        }
        // 获取文件扩展名
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == originalFilename.length() - 1) {
            throw new BusinessException("头像文件格式不正确");
        }
        // 返回文件扩展名
        return originalFilename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * =====================================================
     * 获取头像目录
     * =====================================================
     */
    private Path getAvatarDirectory() {
        return Paths.get(avatarStorageProperties.getAvatarDir()).toAbsolutePath().normalize();
    }

    /**
     * =====================================================
     * URL生成
     * =====================================================
     */
    private String buildAvatarUrl(String fileName) {
        String prefix = avatarStorageProperties.getAvatarUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "/uploads/avatars/";
        }
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix + fileName;
    }

    private String getAvatarValue(SysUser user) {
        try {
            // 获取用户头像字段值
            Object value = org.apache.ibatis.reflection.SystemMetaObject.forObject(user).getValue("avatar");
            return value == null ? null : String.valueOf(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * =====================================================
     * 删除旧的本地头像
     * =====================================================
     */
    private void deleteOldLocalAvatar(String oldAvatar, String newFileName) {
        // 校验旧头像是否为空
        if (oldAvatar == null || oldAvatar.isBlank()) {
            return;
        }
        // 获取头像URL前缀
        String prefix = avatarStorageProperties.getAvatarUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "/uploads/avatars/";
        }
        // 确保前缀以 / 开头
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        /**
         * 非本地头像不删除。
         */
        if (!oldAvatar.startsWith(prefix)) {
            return;
        }
        // 获取旧头像文件名
        String oldFileName = oldAvatar.substring(prefix.length());
        if (oldFileName.isBlank()) {
            return;
        }
        /**
         * 当前新文件不能删。
         */
        if (newFileName != null && newFileName.equals(oldFileName)) {
            return;
        }
        // 获取头像目录
        Path directory = getAvatarDirectory();
        // 构建旧头像路径
        Path oldPath = directory.resolve(oldFileName).normalize();
        if (!oldPath.startsWith(directory)) {
            return;
        }
        // 删除旧头像文件
        deleteFileQuietly(oldPath);
    }

    /**
     * 静默删除。
     * 删除旧头像失败不能影响主业务。
     */
    private void deleteFileQuietly(Path path) {
        try {
            // 静默删除
            Files.deleteIfExists(path);
        }
        catch (IOException ignored) {
            // 不影响头像主业务
        }
    }
}