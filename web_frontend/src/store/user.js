import { reactive } from 'vue'

const state = reactive({
  token: localStorage.getItem('token') || '',
  number: localStorage.getItem('number') || '',
  usrName: localStorage.getItem('usrName') || '',
  role: localStorage.getItem('role') || '',
})

export const userStore = {
  state,
  isLoggedIn() {
    return !!state.token
  },
  setLogin({ token, number, usrName, role }) {
    state.token = token
    state.number = number
    state.usrName = usrName
    state.role = role
    localStorage.setItem('token', token)
    localStorage.setItem('number', number)
    localStorage.setItem('usrName', usrName)
    localStorage.setItem('role', role)
  },
  clear() {
    state.token = ''
    state.number = ''
    state.usrName = ''
    state.role = ''
    localStorage.removeItem('token')
    localStorage.removeItem('number')
    localStorage.removeItem('usrName')
    localStorage.removeItem('role')
  },
}
