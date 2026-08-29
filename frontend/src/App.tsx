import { useEffect, useMemo, useState } from 'react'
import { useAuth0 } from '@auth0/auth0-react'
import { fetchCities, fetchForecast } from './api/weatherApi'
import type { CityWeatherResult, ComfortCategory } from './types/weather'
import { CityCard } from './components/CityCard'
import { LoginButton, LogoutButton } from './auth/AuthButtons'
import { ThemeToggle } from './components/ThemeToggle'
import { DashboardToolbar, type SortKey } from './components/DashboardToolbar'
import { Footer } from './components/Footer'
import { useTheme } from './hooks/useTheme'
import './App.css'
import './components/DashboardToolbar.css'
import './components/TemperatureTrendChart.css'

export default function App() {
  const { isLoading: authLoading, isAuthenticated, getAccessTokenSilently, error: authError } = useAuth0()
  const { theme, toggleTheme } = useTheme()

  const [cities, setCities] = useState<CityWeatherResult[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const [sortKey, setSortKey] = useState<SortKey>('rank')
  const [activeCategory, setActiveCategory] = useState<ComfortCategory | 'All'>('All')
  const [search, setSearch] = useState('')

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

  const displayedCities = useMemo(() => {
    if (!cities) return []

    let result = cities

    if (activeCategory !== 'All') {
      result = result.filter((c) => c.comfort.category === activeCategory)
    }

    if (search.trim()) {
      const q = search.trim().toLowerCase()
      result = result.filter((c) => c.cityName.toLowerCase().includes(q))
    }

    const sorted = [...result]
    switch (sortKey) {
      case 'score':
        sorted.sort((a, b) => b.comfort.score - a.comfort.score)
        break
      case 'temperature':
        sorted.sort((a, b) => b.temperatureCelsius - a.temperatureCelsius)
        break
      case 'name':
        sorted.sort((a, b) => a.cityName.localeCompare(b.cityName))
        break
      case 'rank':
      default:
        sorted.sort((a, b) => a.rank - b.rank)
        break
    }
    return sorted
  }, [cities, activeCategory, search, sortKey])

  async function loadForecast(cityCode: number) {
    const token = await getAccessTokenSilently()
    return fetchForecast(cityCode, token)
  }

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
        <ThemeToggle theme={theme} onToggle={toggleTheme} />
        <div className="login-panel">
          <p className="app__eyebrow">Weather Comfort Analytics</p>
          <h1 className="app__title">Sign in to view the dashboard</h1>
          <p className="app__subtitle">
            This dashboard is restricted to authorized users. Log in with your whitelisted
            account to see live comfort rankings.
          </p>
          <LoginButton />
        </div>
        <Footer />
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
        <div className="app__header-actions">
          <ThemeToggle theme={theme} onToggle={toggleTheme} />
          <LogoutButton />
        </div>
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

      {!loading && !error && cities && cities.length > 0 && (
        <>
          <DashboardToolbar
            sortKey={sortKey}
            onSortKeyChange={setSortKey}
            activeCategory={activeCategory}
            onCategoryChange={setActiveCategory}
            search={search}
            onSearchChange={setSearch}
            resultCount={displayedCities.length}
          />

          {displayedCities.length === 0 ? (
            <div className="app__state">No cities match your filters.</div>
          ) : (
            <div className="city-grid">
              {displayedCities.map((city) => (
                <CityCard key={city.cityCode} city={city} onLoadForecast={loadForecast} />
              ))}
            </div>
          )}
        </>
      )}

      {!loading && !error && cities && cities.length === 0 && (
        <div className="app__state">No cities configured yet.</div>
      )}

      <Footer />
    </div>
  )
}
