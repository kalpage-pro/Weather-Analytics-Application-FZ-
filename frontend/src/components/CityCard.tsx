import { useState } from 'react'
import type { CityWeatherResult, ForecastPoint } from '../types/weather'
import { ComfortGauge } from './ComfortGauge'
import { TemperatureTrendChart } from './TemperatureTrendChart'
import './CityCard.css'

interface CityCardProps {
  city: CityWeatherResult
  onLoadForecast: (cityCode: number) => Promise<ForecastPoint[]>
}

export function CityCard({ city, onLoadForecast }: CityCardProps) {
  const [expanded, setExpanded] = useState(false)
  const [forecast, setForecast] = useState<ForecastPoint[] | null>(null)
  const [loadingForecast, setLoadingForecast] = useState(false)
  const [forecastError, setForecastError] = useState<string | null>(null)

  async function handleToggle() {
    const next = !expanded
    setExpanded(next)
    if (next && forecast === null && !loadingForecast) {
      setLoadingForecast(true)
      setForecastError(null)
      try {
        const data = await onLoadForecast(city.cityCode)
        setForecast(data)
      } catch (err) {
        setForecastError(err instanceof Error ? err.message : 'Could not load forecast.')
      } finally {
        setLoadingForecast(false)
      }
    }
  }

  return (
    <article className="city-card">
      <div className="city-card__header">
        <span className="city-card__rank" aria-label={`Rank ${city.rank}`}>
          {String(city.rank).padStart(2, '0')}
        </span>
        <div className="city-card__title">
          <h2 className="city-card__name">{city.cityName}</h2>
          <p className="city-card__description">{city.weatherDescription}</p>
        </div>
      </div>

      <div className="city-card__temp">
        <span className="city-card__temp-value">{Math.round(city.temperatureCelsius)}</span>
        <span className="city-card__temp-unit">&deg;C</span>
      </div>

      <dl className="city-card__stats">
        <div className="city-card__stat">
          <dt>Humidity</dt>
          <dd>{city.humidity}%</dd>
        </div>
        <div className="city-card__stat">
          <dt>Wind</dt>
          <dd>{city.windSpeedMs.toFixed(1)} m/s</dd>
        </div>
        <div className="city-card__stat">
          <dt>Cloud</dt>
          <dd>{city.cloudinessPct}%</dd>
        </div>
      </dl>

      <ComfortGauge score={city.comfort.score} category={city.comfort.category} />

      <button className="city-card__forecast-toggle" onClick={handleToggle}>
        {expanded ? 'Hide trend' : 'Show temperature trend'}
      </button>

      {expanded && (
        <div className="city-card__forecast">
          {loadingForecast && <p className="trend-chart__empty">Loading forecast&hellip;</p>}
          {forecastError && <p className="trend-chart__empty">{forecastError}</p>}
          {forecast && <TemperatureTrendChart points={forecast} />}
        </div>
      )}
    </article>
  )
}
