import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest {

    @Test
    void split_message() {
        String[] expectedOutput = {"createaccount", "testuser01", "test1@test.gg", "amazingpassword"};
        String[] methodOutput = Client.split_message("createaccount%10testuser01%13test1@test.gg%15amazingpassword");
        assertArrayEquals(methodOutput, expectedOutput);

        expectedOutput = new String[]{"senddirectmessage", "testuser02", "Right Now", "Hello testuser02 from testuser01"};
        methodOutput = Client.split_message("senddirectmessage%10testuser02%09Right Now%032Hello testuser02 from testuser01");
        assertArrayEquals(methodOutput, expectedOutput);
    }

    @Test
    void format_message() {
        String expectedOutput = "createaccount%10testuser01%13test1@test.gg%15amazingpassword";
        String methodOutput = Client.format_message(new int[]{0, 2, 2, 2}, new String[]{"createaccount", "testuser01", "test1@test.gg", "amazingpassword"});
        assertEquals(methodOutput, expectedOutput);
    }


}