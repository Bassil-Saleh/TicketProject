/**
 * SignUpPage is for implementing the page where
 * an event host can create a new account.
 * @returns JSX for the site's sign up page
 */
export function SignUpPage() {
    // TODO: If the user is already logged into the site and
    //       they navigate to this page, then they should be
    //       automatically redirected to the dashboard page.

    // TODO: When the user clicks "Create Account", have the
    //       page send a request to the route for creating a
    //       new event host account.

    // TODO: After receiving a successful response from the
    //       route to create a new event host account, show
    //       the user a message saying that a verification
    //       link has been sent to their email address, and
    //       how long the verification link is valid for.

    // TODO: If an error occurs when the user attempts to
    //       create a new event host account, show an
    //       appropriate error message.

    return (
        <div>
            <h1>Sign Up</h1>
            <form>
                <label>First name</label><br></br>
                <input type="text"></input><br></br>
                <label>Middle name (optional)</label><br></br>
                <input type="text"></input><br></br>
                <label>Last name</label><br></br>
                <input type="text"></input><br></br>
                <label>Date of birth</label><br></br>
                <input type="date"></input><br></br>
                <label>Email address</label><br></br>
                <input type="email"></input><br></br>
                <label>Password</label><br></br>
                <input type="password"></input><br></br>
                <input type="submit" value="Create Account"></input>
            </form>
        </div>
    );
}