/**
 * ProfilePage is for implementing the page where a logged in event host
 * can view and edit their profile info.
 * @returns JSX for the site's profile page
 */
export function ProfilePage() {
    // TODO: If the user is not currently logged in and they
    //       navigate to this page, then they should be
    //       automatically redirected to the home page.

    // TODO: Add functionality to this page using the API routes
    //       for fetching and editing profile info as reference.

    // TODO: When the user clicks "Edit" next to a field,
    //       show a form consisting of a text field and a
    //       "Save" button to let the user edit a specific
    //       profile field.

    return (
        <div>
            <h1>Your Profile</h1>
            <p>Name: </p><button>Edit</button>
            <p>Email Address: </p><button>Edit</button>
        </div>
    );
}