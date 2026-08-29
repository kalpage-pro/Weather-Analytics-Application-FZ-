import type { ComfortCategory } from '../types/weather'

const CATEGORY_COLOR: Record<ComfortCategory, string> = {
  'Very Comfortable': 'var(--color-comfortable)',
  Comfortable: 'var(--color-comfortable)',
  Moderate: 'var(--color-moderate)',
  Uncomfortable: 'var(--color-uncomfortable)',
}

interface ComfortGaugeProps {
  score: number
  category: ComfortCategory
}

/**
 * Renders the comfort score as a horizontal gauge strip with a marker,
 * evoking a barometer/instrument readout rather than a plain progress bar.
 */
export function ComfortGauge({ score, category }: ComfortGaugeProps) {
  const color = CATEGORY_COLOR[category]
  const clamped = Math.max(0, Math.min(100, score))

  return (
    <div className="comfort-gauge" role="img" aria-label={`Comfort score ${score} out of 100, ${category}`}>
      <div className="comfort-gauge__track">
        <div className="comfort-gauge__ticks" aria-hidden="true">
          {Array.from({ length: 21 }).map((_, i) => (
            <span key={i} className="comfort-gauge__tick" />
          ))}
        </div>
        <div
          className="comfort-gauge__fill"
          style={{ width: `${clamped}%`, background: color }}
        />
        <div
          className="comfort-gauge__marker"
          style={{ left: `${clamped}%`, borderColor: color }}
        />
      </div>
      <div className="comfort-gauge__readout">
        <span className="comfort-gauge__score" style={{ color }}>
          {score.toFixed(1)}
        </span>
        <span className="comfort-gauge__category">{category}</span>
      </div>
    </div>
  )
}
