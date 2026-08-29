import type { ComfortCategory } from '../types/weather'

export type SortKey = 'rank' | 'score' | 'temperature' | 'name'

const CATEGORIES: (ComfortCategory | 'All')[] = [
  'All',
  'Very Comfortable',
  'Comfortable',
  'Moderate',
  'Uncomfortable',
]

interface DashboardToolbarProps {
  sortKey: SortKey
  onSortKeyChange: (key: SortKey) => void
  activeCategory: ComfortCategory | 'All'
  onCategoryChange: (category: ComfortCategory | 'All') => void
  search: string
  onSearchChange: (value: string) => void
  resultCount: number
}

export function DashboardToolbar({
  sortKey,
  onSortKeyChange,
  activeCategory,
  onCategoryChange,
  search,
  onSearchChange,
  resultCount,
}: DashboardToolbarProps) {
  return (
    <div className="toolbar">
      <div className="toolbar__row">
        <input
          type="text"
          className="toolbar__search"
          placeholder="Search city..."
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
          aria-label="Search cities"
        />

        <label className="toolbar__sort">
          <span>Sort</span>
          <select
            value={sortKey}
            onChange={(e) => onSortKeyChange(e.target.value as SortKey)}
            aria-label="Sort cities by"
          >
            <option value="rank">Rank</option>
            <option value="score">Comfort score</option>
            <option value="temperature">Temperature</option>
            <option value="name">City name</option>
          </select>
        </label>
      </div>

      <div className="toolbar__chips" role="group" aria-label="Filter by comfort category">
        {CATEGORIES.map((category) => (
          <button
            key={category}
            className={`toolbar__chip${activeCategory === category ? ' toolbar__chip--active' : ''}`}
            onClick={() => onCategoryChange(category)}
          >
            {category}
          </button>
        ))}
      </div>

      <p className="toolbar__count">
        Showing {resultCount} {resultCount === 1 ? 'city' : 'cities'}
      </p>
    </div>
  )
}
