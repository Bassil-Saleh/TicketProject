/**
 * ResetPasswordPage is for implementing the page where a user can
 * reset their password using a password reset token sent to them by email.
 * @returns JSX for the reset password page
 */
export function ResetPasswordPage() {
    // TODO: Add functionality to this page using the API route for
    //       using a password reset token as reference.

    // TODO: Show an appropriate message to the user based on
    //       the request's result (success, failure, etc.).

    // TODO: Add CSS styling.

    return (
        <div>
            <p>Fill out the below form to reset your account's password.</p>
            <form>
                <label>Password Reset Token:</label><br></br>
                <input type="text"></input><br></br>
                <label>New Password:</label><br></br>
                <input type="password"></input><br></br>
                <label>Confirm Password:</label><br></br>
                <input type="password"></input><br></br>
                <input type="submit" value="Reset Password"></input>
            </form>
        </div>
    );
}