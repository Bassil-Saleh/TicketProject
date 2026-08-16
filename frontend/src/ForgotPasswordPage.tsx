import { useState, type SubmitEvent } from 'react';
import { Link } from 'react-router-dom';

/**
 * ForgotPasswordPage is for implementing the page where a user can
 * enter their email address and receive a password reset token to
 * their email address.
 * @returns JSX for the site's forgot password page.
 */
export function ForgotPasswordPage() {
    const [email, setEmail] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    /**
     * Handles the forgot password form submission. Calls the
     * create password reset token API endpoint. On success,
     * shows a confirmation message. On failure, displays an
     * error message.
     */
    const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');
        setIsSubmitting(true);

        try {
            const response = await fetch('/api/v1/password-reset-tokens', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email }),
            });

            const data = await response.json();

            if (response.ok) {
                setSuccessMessage(
                    'If that email address is associated with an account, a ' +
                    'password reset link has been sent to it. The link is ' +
                    'valid for 1 hour. Please check your inbox.'
                );
            } else {
                setErrorMessage(data.message || 'Failed to send password reset email. Please try again.');
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
                <h1 className="auth-card__title">Forgot Password</h1>
                <p className="auth-card__subtitle">
                    Enter your email address below to receive a password
                    reset link.
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
                        <label className="form-label" htmlFor="forgot-email">
                            Email Address
                        </label>
                        <input
                            id="forgot-email"
                            className="form-input"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="john.doe@example.com"
                            required
                            autoComplete="email"
                        />
                    </div>

                    <button
                        type="submit"
                        className="btn btn--primary btn--block"
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? 'Sending...' : 'Send Reset Link'}
                    </button>
                </form>

                <div className="auth-card__footer">
                    Remember your password?{' '}
                    <Link to="/login">Log In</Link>
                </div>
            </div>
        </main>
    );
}