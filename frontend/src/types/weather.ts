export type ComfortCategory =
  | 'Very Comfortable'
  | 'Comfortable'
  | 'Moderate'
  | 'Uncomfortable'

export interface ComfortResult {
  score: number
  category: ComfortCategory
  tempPenalty: number
  humidityPenalty: number
  windPenalty: number
  cloudPenalty: number
}

export interface CityWeatherResult {
  cityCode: number
  cityName: string
  weatherDescription: string
  temperatureCelsius: number
  humidity: number
  windSpeedMs: number
  cloudinessPct: number
  comfort: ComfortResult
  rank: number
}

export interface ForecastPoint {
  time: string
  temperatureCelsius: number
}
