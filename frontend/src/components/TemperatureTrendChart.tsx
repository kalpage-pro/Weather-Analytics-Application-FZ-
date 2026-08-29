import type { ForecastPoint } from '../types/weather'

interface TemperatureTrendChartProps {
  points: ForecastPoint[]
}

const WIDTH = 240
const HEIGHT = 70
const PADDING = 8

export function TemperatureTrendChart({ points }: TemperatureTrendChartProps) {
  if (points.length < 2) {
    return <p className="trend-chart__empty">Not enough forecast data yet.</p>
  }

  const temps = points.map((p) => p.temperatureCelsius)
  const min = Math.min(...temps)
  const max = Math.max(...temps)
  const range = max - min || 1

  const stepX = (WIDTH - PADDING * 2) / (points.length - 1)

  const coords = points.map((p, i) => {
    const x = PADDING + i * stepX
    const y = HEIGHT - PADDING - ((p.temperatureCelsius - min) / range) * (HEIGHT - PADDING * 2)
    return { x, y, temp: p.temperatureCelsius, time: p.time }
  })

  const linePath = coords.map((c, i) => `${i === 0 ? 'M' : 'L'} ${c.x.toFixed(1)} ${c.y.toFixed(1)}`).join(' ')
  const areaPath = `${linePath} L ${coords[coords.length - 1].x.toFixed(1)} ${HEIGHT - PADDING} L ${coords[0].x.toFixed(1)} ${HEIGHT - PADDING} Z`

  return (
    <div className="trend-chart">
      <svg
        viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
        className="trend-chart__svg"
        role="img"
        aria-label={`Temperature trend over the next ${points.length * 3} hours, from ${min.toFixed(0)} to ${max.toFixed(0)} degrees`}
      >
        <path d={areaPath} className="trend-chart__area" />
        <path d={linePath} className="trend-chart__line" />
        {coords.map((c, i) => (
          <circle key={i} cx={c.x} cy={c.y} r={2.5} className="trend-chart__point" />
        ))}
      </svg>
      <div className="trend-chart__labels">
        <span>{min.toFixed(0)}&deg;</span>
        <span className="trend-chart__labels-mid">next {points.length * 3}h</span>
        <span>{max.toFixed(0)}&deg;</span>
      </div>
    </div>
  )
}
