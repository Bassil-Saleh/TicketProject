/**
 * DeleteAccountPage is for implementing the page that lets
 * a logged in event host delete their account.
 * @returns JSX for the site's delete account page
 */
export function DeleteAccountPage() {
    // TODO: Add CSS styling.

    // TODO: If the user visits this page but they are
    //       not currently logged in, then they should
    //       be automatically redirected to the home page.

    // TODO: Add logic to let the logged in event host delete
    //       their account, using the DELETE API route to delete
    //       an event host account as reference.

    // TODO: Show an appropriate message based on the result of
    //       the account deletion request (success, failure, etc.).

    // TODO: After successfully deleting an account, the user should
    //       be automatically redirected to the home page and their
    //       current login session should be discarded.

    return (
        <div>
            <h1>Account Deletion</h1>
            <p>
                Deleting your account removes all:
            </p>
            <ul>
                <li>Events you have created.</li>
                <li>Registrations/invitations created for those events.</li>
                <li>Records of tickets you've scanned over the course of your account's life.</li>
                <li>Login session tokens for your account.</li>
                <li>Password reset tokens for your account.</li>
            </ul>
            <p>
                Please be certain you understand this before you proceed with deleting your account.
            </p>
            <p>
                Before you attempt to delete your account, ensure that
                you do not currently have any pending or ongoing events,
                as they would need to either be finished or canceled
                before deleting your account.
            </p>
            <form>
                <label>
                    I understand the effects of deleting my account and I wish to proceed.
                </label><br></br>
                <input type="checkbox"></input><br></br>
                <input type="submit" value="Delete My Account"></input>
            </form>
        </div>
    );
}