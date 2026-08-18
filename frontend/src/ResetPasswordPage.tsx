import { useState, type SubmitEvent } from 'react';
import { Link, useSearchParams } from 'react-router-dom';

/**
 * ResetPasswordPage is for implementing the page where a user can
 * reset their password using a password reset token sent to them by email.
 * @returns JSX for the reset password page
 */
export function ResetPasswordPage() {
    const [searchParams] = useSearchParams();

    // Pre-fill the token from the URL query parameter
    // (e.g. /reset-password?token=abc123).
    const token = searchParams.get('token');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    /**
     * Handles the reset password form submission. Validates that
     * the password and confirm password fields match, then calls
     * the use password reset token API endpoint. On success, shows
     * a confirmation message. On failure, displays an error message.
     */
    const handleSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();
        setErrorMessage('');
        setSuccessMessage('');

        // Client-side validation: passwords must match.
        if (password !== confirmPassword) {
            setErrorMessage('Passwords do not match. Please try again.');
            return;
        }

        setIsSubmitting(true);

        try {
            const response = await fetch('/api/v1/password-reset-tokens', {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    passwordResetToken: token,
                    password,
                }),
            });

            const data = await response.json();

            if (response.ok) {
                setSuccessMessage(
                    'Your password has been reset successfully. You can now ' +
                    'log in with your new password.'
                );
            } else {
                setErrorMessage(data.message || 'Failed to reset password. Please try again.');
            }
        } catch {
            setErrorMessage('An unexpected error occurred. Please try again.');
        } finally {
            setIsSubmitting(false);
        }
    };

    // There must be a token supplied as a URL parameter in order
    // to reset an account's password.
    if (!token) {
        return (
            <main className="auth-page">
                <div className="auth-card">
                    <h1 className="auth-card__title">Reset Password</h1>
                    <div className="alert alert--error" role="alert">
                        The URL is missing a valid password reset token paramater.
                    </div>
                </div>
            </main>
        );
    }

    return (
        <main className="auth-page">
            <div className="auth-card">
                <h1 className="auth-card__title">Reset Password</h1>
                <p className="auth-card__subtitle">
                    Enter your new password below.
                </p>

                {errorMessage && (
                    <div className="alert alert--error" role="alert">
                        {errorMessage}
                    </div>
                )}

                {successMessage && (
                    <>
                        <div className="alert alert--success" role="alert">
                            {successMessage}
                        </div>
                        <div className="auth-card__footer">
                            <Link to="/login" className="btn btn--primary">
                                Go to Log In
                            </Link>
                        </div>
                    </>
                )}

                {!successMessage && (
                    <form onSubmit={handleSubmit} noValidate>
                        <div className="form-group">
                            <label className="form-label" htmlFor="reset-password">
                                New Password
                            </label>
                            <input
                                id="reset-password"
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

                        <div className="form-group">
                            <label className="form-label" htmlFor="reset-confirm-password">
                                Confirm Password
                            </label>
                            <input
                                id="reset-confirm-password"
                                className="form-input"
                                type="password"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                placeholder="Re-enter your new password"
                                required
                                autoComplete="new-password"
                            />
                        </div>

                        <button
                            type="submit"
                            className="btn btn--primary btn--block"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? 'Resetting...' : 'Reset Password'}
                        </button>
                    </form>
                )}

                <div className="auth-card__footer">
                    Remember your password?{' '}
                    <Link to="/login">Log In</Link>
                </div>
            </div>
        </main>
    );
}