import type { CityWeatherResult } from '../types/weather'

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
