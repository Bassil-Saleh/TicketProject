import { Routes, Route } from 'react-router-dom';
import { HomePage } from './HomePage.tsx';
import { NavigationBar } from './NavigationBar.tsx';
import { Footer } from './Footer.tsx';
import { LoginPage } from './LoginPage.tsx';
import { SignUpPage } from './SignUpPage.tsx';
import { Dashboard } from './Dashboard.tsx';
import { ProfilePage } from './ProfilePage.tsx';
import { ForgotPasswordPage } from './ForgotPasswordPage.tsx';
import { ResetPasswordPage } from './ResetPasswordPage.tsx';
import { AccountVerificationPage } from './AccountVerificationPage.tsx';

/**
 * App is the component from which all the other
 * components branch off from. It defines the client-side
 * routes for every page in the application.
 * @returns JSX for the application's route tree.
 */
function App() {
  return (
    <div>
      <NavigationBar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/verify-account" element={<AccountVerificationPage />} />
      </Routes>
      <Footer />
    </div>
  );
}

export default App