export function SignUpPage() {
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
                <input type="submit" value="Sign Up"></input>
            </form>
        </div>
    );
}