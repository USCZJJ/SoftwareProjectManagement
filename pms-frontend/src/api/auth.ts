import request from '@/utils/request'

export function login(username: string, password: string) {
  return request({ url: '/auth/login', method: 'post', data: { username, password } })
}

export function getUserInfo() {
  return request({ url: '/auth/user-info', method: 'get' })
}
