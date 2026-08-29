export function Footer() {
  const year = new Date().getFullYear()
  return (
    <footer className="footer">
      <p className="footer__text">
        Weather Comfort Analytics by minidu &copy; {year} &mdash; live data from OpenWeatherMap,
        secured with Auth0.
      </p>
    </footer>
  )
}
