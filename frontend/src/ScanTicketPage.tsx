/**
 * ScanTicketPage is for implementing the page that lets a
 * logged in event host scan a QR code ticket using their
 * device's camera (webcam, smartphone camera, etc.)
 * going through their web browser.
 * @returns JSX for the site's ticket scanning page
 */
export function ScanTicketPage() {
    // TODO: If the user is not currently logged in, automatically
    //       redirect them to the home page.

    // TODO: If the user is currently logged in, request access to
    //       their device's camera through their web browser.

    // TODO: If the page cannot get camera permissions from the
    //       web browser, show a message saying that QR codes
    //       cannot be scanned without access to a camera.

    // TODO: If the page has camera permissions from the web browser
    //       but no camera device is found, show a message saying that
    //       no camera has been found on the user's device.

    // TODO: If the page has camera permissions and at least one
    //       camera device is found, let the user pick a camera
    //       to use for scanning QR codes.

    // TODO: Once the user has selected a working camera on their device,
    //       show the camera feed to the user on the web page.

    // TODO: After successfully detecting and scanning a QR code,
    //       send a request to the API route POST /api/v1/ticket-scans
    //       containing the scanned value.

    // TODO: Show an appropriate message to the user based on the
    //       result of the request (success, failure, etc.).

    return (
        <div></div>
    );
}