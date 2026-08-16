import { useState, useEffect, type SubmitEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext.tsx';

/**
 * SignUpPage is for implementing the page where
 * an event host can create a new account.
 * @returns JSX for the site's sign up page
 */
export function SignUpPage() {
    const { isLoggedIn } = useAuth();
    const navigate = useNavigate();

    const [firstName, setFirstName] = useState('');
    const [middleName, setMiddleName] = useState('');
    const [lastName, setLastName] = useState('');
    const [dateOfBirth, setDateOfBirth] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    // If the user is already logged in, redirect to the dashboard.
    useEffect(() => {
        if (isLoggedIn) {
            navigate('/dashboard', { replace: true });
        }
    }, [isLoggedIn, navigate]);

    /**
     * Handles the sign up form submission. Calls the create event
     * host API endpoint. On success, shows a message about the
     * verification email. On failure, displays an error message.
     */
    const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');
        setIsSubmitting(true);

        try {
            const requestBody: Record<string, string> = {
                firstName,
                lastName,
                email,
                password,
                dateOfBirth,
            };
            // middleName is optional; only include it if provided.
            if (middleName.trim() !== '') {
                requestBody.middleName = middleName;
            }

            const response = await fetch('/api/v1/event-hosts', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(requestBody),
            });

            const data = await response.json();

            if (response.ok) {
                setSuccessMessage(
                    'Your account has been created! A verification link has been ' +
                    'sent to your email address. The link is valid for 1 hour. ' +
                    'Please verify your account before logging in.'
                );
            } else {
                setErrorMessage(data.message || 'Failed to create account. Please try again.');
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
                <h1 className="auth-card__title">Sign Up</h1>
                <p className="auth-card__subtitle">
                    Create your free event host account.
                </p>

                {errorMessage && (
                    <div className="alert alert--error" role="alert">
                        {errorMessage}
                    </div>
                )}

                {successMessage && (
                    <div className="alert alert--success" role="alert">
                        {successMessage}
                    </div>
                )}

                <form onSubmit={handleSubmit} noValidate>
                    <div className="form-group">
                        <label className="form-label" htmlFor="signup-first-name">
                            First Name
                        </label>
                        <input
                            id="signup-first-name"
                            className="form-input"
                            type="text"
                            value={firstName}
                            onChange={(e) => setFirstName(e.target.value)}
                            placeholder="John"
                            required
                            autoComplete="given-name"
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="signup-middle-name">
                            Middle Name (optional)
                        </label>
                        <input
                            id="signup-middle-name"
                            className="form-input"
                            type="text"
                            value={middleName}
                            onChange={(e) => setMiddleName(e.target.value)}
                            placeholder="Michael"
                            autoComplete="additional-name"
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="signup-last-name">
                            Last Name
                        </label>
                        <input
                            id="signup-last-name"
                            className="form-input"
                            type="text"
                            value={lastName}
                            onChange={(e) => setLastName(e.target.value)}
                            placeholder="Doe"
                            required
                            autoComplete="family-name"
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="signup-dob">
                            Date of Birth
                        </label>
                        <input
                            id="signup-dob"
                            className="form-input"
                            type="date"
                            value={dateOfBirth}
                            onChange={(e) => setDateOfBirth(e.target.value)}
                            required
                            autoComplete="bday"
                        />
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="signup-email">
                            Email Address
                        </label>
                        <input
                            id="signup-email"
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
                        <label className="form-label" htmlFor="signup-password">
                            Password
                        </label>
                        <input
                            id="signup-password"
                            className="form-input"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="At least 12 characters"
                            required
                            minLength={12}
                            maxLength={128}
                            autoComplete="new-password"
                        />
                        <p className="form-hint">
                            Must be between 12 and 128 characters.
                        </p>
                    </div>

                    <button
                        type="submit"
                        className="btn btn--primary btn--block"
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? 'Creating Account...' : 'Create Account'}
                    </button>
                </form>

                <div className="auth-card__footer">
                    Already have an account?{' '}
                    <Link to="/login">Log In</Link>
                </div>
            </div>
        </main>
    );
}