/**
 * ForgotPasswordPage is for implementing the page where a user can
 * enter their email address and receive a password reset token to
 * their email address.
 * @returns JSX for the site's forgot password page.
 */
export function ForgotPasswordPage() {
    // TODO: Add functionality to this page using the API route for
    //       requesting a password reset as reference.

    // TODO: Show an appropriate message to the user based on the
    //       request's result (success, failure, etc.).

    // TODO: Add CSS styling.

    return (
        <div>
            <p>Enter your email address in the below form to receive a password reset token.</p>
            <form>
                <label>Email Address:</label><br></br>
                <input type="email"></input><br></br>
                <input type="submit" value="Submit"></input>
            </form>
        </div>
    );
}