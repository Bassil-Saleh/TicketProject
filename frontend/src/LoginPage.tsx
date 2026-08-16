/**
 * LoginPage is for implementing the page where an event host
 * can log into their account. 
 * @returns JSX for the site's login page
 */
export function LoginPage() {
    // TODO: If the user is currently logged in and they navigate
    //       to this page, then they should be automatically
    //       redirected to the dashboard page.

    // TODO: When the user clicks "Log In", have the page send a
    //       request to the API route for creating login sessions.

    // TODO: If the user successfully logs in, then they should be
    //       automatically redirected to the dashboard page.

    // TODO: If the log in fails, show an appropriate error message.

    return (
        <div>
            <h1>Log In</h1>
            <form>
                <label>Email Address:</label><br></br>
                <input type="email"></input><br></br>
                <label>Password:</label><br></br>
                <input type="password"></input><br></br>
                <input type="submit" value="Log In"></input>
            </form>
            <a>Forgot Password?</a>
        </div>
    );
}