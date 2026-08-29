import type { CityWeatherResult, ForecastPoint } from '../types/weather'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

export async function fetchCities(token: string): Promise<CityWeatherResult[]> {
  const res = await fetch(`${API_BASE}/api/cities`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
  if (!res.ok) {
    throw new Error(`Failed to load cities: ${res.status} ${res.statusText}`)
  }
  return res.json()
}

export async function fetchForecast(cityCode: number, token: string): Promise<ForecastPoint[]> {
  const res = await fetch(`${API_BASE}/api/cities/${cityCode}/forecast`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
  if (!res.ok) {
    throw new Error(`Failed to load forecast: ${res.status} ${res.statusText}`)
  }
  return res.json()
}
