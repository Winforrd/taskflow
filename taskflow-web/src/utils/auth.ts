const TOKEN_KEY = 'taskflow-token'


/**
 * 获取 Token
 */
export function getToken(): string | null {

  return localStorage.getItem(TOKEN_KEY)

}


/**
 * 保存 Token
 */
export function setToken(token: string): void {

  localStorage.setItem(TOKEN_KEY, token)

}


/**
 * 删除 Token
 */
export function removeToken(): void {

  localStorage.removeItem(TOKEN_KEY)

}