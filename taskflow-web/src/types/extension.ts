/**
 * =========================================================
 * TaskFlow扩展能力
 * =========================================================
 */
export type ExtensionCode =
  | 'APPROVAL'
  | 'CALENDAR'
  | 'WORKTIME'
  | 'SMART_ASSIGNMENT'


/**
 * 扩展能力信息。
 */
export interface ExtensionCapability {

  /**
   * 扩展编码。
   */
  code: ExtensionCode

  /**
   * 名称。
   */
  name: string

  /**
   * 功能说明。
   */
  description: string

  /**
   * 当前是否启用。
   */
  enabled: boolean
}