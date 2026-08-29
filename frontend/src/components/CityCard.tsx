import type { CityWeatherResult } from '../types/weather'
import { ComfortGauge } from './ComfortGauge'
import './CityCard.css'

interface CityCardProps {
  city: CityWeatherResult
}

export function CityCard({ city }: CityCardProps) {
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
    </article>
  )
}
