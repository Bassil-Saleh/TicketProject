/**
 * NavigationBar is for implementing a navigation bar that should
 * always be visible on the top of each web page. Its visible elements
 * should be different based on whether or not a user is currently logged in.
 * @returns JSX for the site's navigation bar.
 */
export function NavigationBar() {
    // TODO: If the user is currently logged in, hide the
    //       "Log In" and "Sign Up" buttons and instead show
    //       "Log Out" and "Dashboard" buttons.

    // TODO: Add CSS styling.

    return (
        <div>
            <h1>TicketProject</h1>
            <div>
                <button>Log In</button>
                <button>Sign Up</button>
            </div>
        </div>
    );
}