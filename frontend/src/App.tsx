import { HomePage } from './HomePage.tsx';
import { NavigationBar } from './NavigationBar.tsx';
import { Footer } from './Footer.tsx';

/**
 * App is the component from which all the other
 * components branch off from.
 * @returns JSX of the first page that the user sees when
 *          the navigate to the site.
 */
function App() {
  return (
    <div>
      <NavigationBar />
        <HomePage />
      <Footer />
    </div>
  );
}

export default App
