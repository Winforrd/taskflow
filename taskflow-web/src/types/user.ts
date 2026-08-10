export interface UserInfo {
  id: bigint

  username: string

  nickname: string

  avatar?: string

  email?: string

  phone?: string

  status: number

  createdTime: string

  updatedTime: string

  deleted: number
}


export interface LoginRequest {

  username: string

  password: string

}


export interface RegisterRequest {

  username: string

  password: string

  nickname: string

  email?: string

}


export interface LoginResult {

  token: string

  userId: bigint

  username: string

  nickname: string

}

export interface UpdateProfileRequest {

  nickname: string

  email?: string

  phone?: string
}


export interface ChangePasswordRequest {

  oldPassword: string

  newPassword: string
}