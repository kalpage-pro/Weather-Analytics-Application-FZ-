import { useAuth0 } from '@auth0/auth0-react'

export function LoginButton() {
  const { loginWithRedirect } = useAuth0()
  return (
    <button className="auth-button auth-button--primary" onClick={() => loginWithRedirect()}>
      Log in
    </button>
  )
}

export function LogoutButton() {
  const { logout, user } = useAuth0()
  return (
    <button
      className="auth-button auth-button--ghost"
      onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
    >
      Log out{user?.email ? ` (${user.email})` : ''}
    </button>
  )
}
