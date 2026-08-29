import { useEffect, useState } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { fetchCities } from './api/weatherApi'
import type { CityWeatherResult } from './types/weather'
import { CityCard } from './components/CityCard'
import { LoginButton, LogoutButton } from './auth/AuthButtons'
import './App.css'

export default function App() {
  const { isLoading: authLoading, isAuthenticated, getAccessTokenSilently, error: authError } = useAuth0()

  const [cities, setCities] = useState<CityWeatherResult[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!isAuthenticated) return

    let cancelled = false
    setLoading(true)

    getAccessTokenSilently()
      .then((token) => fetchCities(token))
      .then((data) => {
        if (!cancelled) {
          setCities(data)
          setLoading(false)
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Something went wrong.')
          setLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [isAuthenticated, getAccessTokenSilently])

  if (authLoading) {
    return (
      <div className="app">
        <div className="app__state" role="status">
          Checking your session&hellip;
        </div>
      </div>
    )
  }

  if (authError) {
    return (
      <div className="app">
        <div className="app__state app__state--error" role="alert">
          <p className="app__state-title">Authentication error.</p>
          <p className="app__state-detail">{authError.message}</p>
        </div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return (
      <div className="app app--centered">
        <div className="login-panel">
          <p className="app__eyebrow">Weather Comfort Analytics</p>
          <h1 className="app__title">Sign in to view the dashboard</h1>
          <p className="app__subtitle">
            This dashboard is restricted to authorized users. Log in with your whitelisted
            account to see live comfort rankings.
          </p>
          <LoginButton />
        </div>
      </div>
    )
  }

  return (
    <div className="app">
      <header className="app__header app__header--with-actions">
        <div>
          <p className="app__eyebrow">Weather Comfort Analytics</p>
          <h1 className="app__title">Where&rsquo;s the air easiest to breathe today?</h1>
          <p className="app__subtitle">
            Live conditions across {cities?.length ?? '—'} cities, ranked by a custom Comfort
            Index that weighs temperature, humidity, wind, and cloud cover together.
          </p>
        </div>
        <LogoutButton />
      </header>

      {loading && (
        <div className="app__state" role="status">
          Reading the instruments&hellip;
        </div>
      )}

      {error && !loading && (
        <div className="app__state app__state--error" role="alert">
          <p className="app__state-title">Couldn&rsquo;t load the dashboard.</p>
          <p className="app__state-detail">{error}</p>
          <p className="app__state-hint">
            Check that the backend is running, your OpenWeatherMap key is set, and your Auth0
            API audience matches on both frontend and backend.
          </p>
        </div>
      )}

      {!loading && !error && cities && cities.length === 0 && (
        <div className="app__state">No cities configured yet.</div>
      )}

      {!loading && !error && cities && cities.length > 0 && (
        <div className="city-grid">
          {cities.map((city) => (
            <CityCard key={city.cityCode} city={city} />
          ))}
        </div>
      )}
    </div>
  )
}
