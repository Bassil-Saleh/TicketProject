import { useState, useEffect, type SubmitEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext.tsx';

/**
 * LoginPage is for implementing the page where an event host
 * can log into their account.
 * @returns JSX for the site's login page
 */
export function LoginPage() {
    const { isLoggedIn, login } = useAuth();
    const navigate = useNavigate();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    // If the user is already logged in, redirect to the dashboard.
    useEffect(() => {
        if (isLoggedIn) {
            navigate('/dashboard', { replace: true });
        }
    }, [isLoggedIn, navigate]);

    /**
     * Handles the login form submission. Calls the login API
     * endpoint and, on success, stores the JWT and redirects
     * to the dashboard. On failure, displays an error message.
     */
    const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setErrorMessage('');
        setIsSubmitting(true);

        try {
            const response = await fetch('/api/v1/sessions/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password }),
            });

            const data = await response.json();

            if (response.ok && data.jwt) {
                login(data.jwt);
                navigate('/dashboard', { replace: true });
            } else {
                setErrorMessage(data.message || 'Login failed. Please check your email and password.');
            }
        } catch {
            setErrorMessage('An unexpected error occurred. Please try again.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <main className="auth-page">
            <div className="auth-card">
                <h1 className="auth-card__title">Log In</h1>
                <p className="auth-card__subtitle">
                    Welcome back! Log in to manage your events.
                </p>

                {errorMessage && (
                    <div className="alert alert--error" role="alert">
                        {errorMessage}
                    </div>
                )}

                <form onSubmit={handleSubmit} noValidate>
                    <div className="form-group">
                        <label className="form-label" htmlFor="login-email">
                            Email Address
                        </label>
                        <input
                            id="login-email"
                            className="form-input"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="john.doe@example.com"
                            required
                            autoComplete="email"
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="login-password">
                            Password
                        </label>
                        <input
                            id="login-password"
                            className="form-input"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="Enter your password"
                            required
                            autoComplete="current-password"
                        />
                    </div>

                    <button
                        type="submit"
                        className="btn btn--primary btn--block"
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? 'Logging In...' : 'Log In'}
                    </button>
                </form>

                <div className="auth-card__footer">
                    <Link to="/forgot-password">Forgot Password?</Link>
                </div>

                <div className="auth-card__footer">
                    Don't have an account?{' '}
                    <Link to="/signup">Sign Up</Link>
                </div>
            </div>
        </main>
    );
}