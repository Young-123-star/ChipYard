import request from '@/utils/request'

export interface LoginVO {
  token: string
  userId: number
  username: string
  realName: string
}

export interface CurrentUser {
  userId: number
  username: string
  realName: string
  gender: number
}

export function login(data: { username: string; password: string }): Promise<LoginVO> {
  return request.post('/auth/login', data)
}

export function getCurrentUser(): Promise<CurrentUser> {
  return request.get('/auth/me')
}
