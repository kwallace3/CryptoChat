import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    @Test
    void split_message() {
        String[] expectedOutput = {"createaccount", "testuser01", "test1@test.gg", "amazingpassword"};
        String[] methodOutput =JavaServer.split_message("createaccount%10testuser01%13test1@test.gg%15amazingpassword");
        assertArrayEquals(methodOutput, expectedOutput);

        expectedOutput = new String[]{"senddirectmessage", "testuser02", "Right Now", "Hello testuser02 from testuser01"};
        methodOutput = JavaServer.split_message("senddirectmessage%10testuser02%09Right Now%032Hello testuser02 from testuser01");
        assertArrayEquals(methodOutput, expectedOutput);
    }

    @Test
    void format_message() {
        String expectedOutput = "createaccount%10testuser01%13test1@test.gg%15amazingpassword";
        String methodOutput = JavaServer.format_message(new int[]{0, 2, 2, 2}, new String[]{"createaccount", "testuser01", "test1@test.gg", "amazingpassword"});
        assertEquals(methodOutput, expectedOutput);
    }

    @Test
    void account_creation(){
        String expectedOutput = "createaccount%7success%08TestUser";
        String methodOutput = Account.create_account("TestUser", "test@test.gg", "AnAmazingPassword");
        assertEquals(methodOutput, expectedOutput);

        expectedOutput = "createaccount%7failure%08TestUser%15TestUser in use";
        methodOutput = Account.create_account("TestUser", "test@test1.gg", "AnAmazingPassword");
        assertEquals(methodOutput, expectedOutput);

        expectedOutput = "createaccount%7failure%09TestUser1%19test@test.gg in use";
        methodOutput = Account.create_account("TestUser1", "test@test.gg", "AnAmazingPassword");
        assertEquals(methodOutput, expectedOutput);
    }

    @Test
    void account_login(){
        String expectedOutput = "login%7success%08TestUser";
        String methodOutput = Account.login("TestUser", "AnAmazingPassword");
        assertEquals(methodOutput, expectedOutput);

        expectedOutput = "login%7failure%08TestUser";
        methodOutput = Account.login("TestUser", "ATerriblePassword");
        assertEquals(methodOutput, expectedOutput);

        expectedOutput = "login%7failure%09TestUser3";
        methodOutput = Account.login("TestUser3", "AnAmazingPassword");
        assertEquals(methodOutput, expectedOutput);
    }


}