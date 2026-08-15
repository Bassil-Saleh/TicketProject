import { HomePage } from './HomePage.tsx';
import { NavigationBar } from './NavigationBar.tsx';
import { Footer } from './Footer.tsx';

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
