/*
 * Copyright (c) 2024. PingIdentity. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package com.pingidentity.davinci

import androidx.test.filters.SmallTest
import com.pingidentity.utils.Result
import com.pingidentity.davinci.collector.FlowCollector
import com.pingidentity.davinci.collector.PasswordCollector
import com.pingidentity.davinci.collector.SubmitCollector
import com.pingidentity.davinci.collector.TextCollector
import com.pingidentity.davinci.module.Oidc
import com.pingidentity.davinci.module.description
import com.pingidentity.davinci.module.name
import com.pingidentity.davinci.plugin.collectors
import com.pingidentity.logger.CONSOLE
import com.pingidentity.logger.Logger
import com.pingidentity.orchestrate.Connector
import com.pingidentity.orchestrate.Failure
import com.pingidentity.orchestrate.Success
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SmallTest
class DavinciAndroidTest {
    private var daVinci: DaVinci =  DaVinci {
        logger = Logger.CONSOLE

        module(Oidc) {
            clientId = "a9cf6b4c-8669-4dee-8b97-7c703752c04f"
            discoveryEndpoint = "https://auth.pingone.ca/02fb4743-189a-4bc7-9d6c-a919edfe6447/as/.well-known/openid-configuration"
            scopes = mutableSetOf("openid", "email", "address", "phone", "profile")
            redirectUri = "org.forgerock.demo://oauth2redirect"
            //storage = dataStore
        }
    }

    private lateinit var userFname: String
    private lateinit var userLname: String
    private lateinit var usernamePrefix: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var verificationCode: String

    @BeforeTest
    fun setUp() = runTest {
        userFname = "E2E"
        userLname = "User"
        usernamePrefix = "e2e"

        // This user must exist in PingOne...
        username = "e2euser@example.com"
        password = "ForgeR0ck#1"
        verificationCode = "1234" // This is hardcoded value in the DaVinci flow

        //Start with a clean session
        daVinci.user()?.logout()
    }

    // C21274
    @Test
    fun loginSuccess() = runTest {
        var node = daVinci.start() // Return first Node
        assertTrue(node is Connector)

        assertTrue { (node as Connector).collectors.size == 1 }
        assertEquals("Start Node", node.name)
        assertEquals("Click next to continue...", node.description)

        val collector = node.collectors[0] as TextCollector
        assertEquals("protectsdk", collector.key)
        assertEquals("Protect Payload", collector.label)

        node = node.next()
        node = node as Connector

        // Login form validation...
        assertTrue(node.collectors.size == 5 )

        assertTrue(node.collectors[0] is TextCollector)
        assertTrue(node.collectors[1] is PasswordCollector)
        assertTrue(node.collectors[2] is SubmitCollector)
        assertTrue(node.collectors[3] is FlowCollector)
        assertTrue(node.collectors[4] is FlowCollector)

        assertEquals("E2E Login Form", node.name)
        assertEquals("Enter your username and password", node.description)
        assertEquals("Username", (node.collectors[0] as TextCollector).label)
        assertEquals("Password", (node.collectors[1] as PasswordCollector).label)
        assertEquals("Sign On", (node.collectors[2] as SubmitCollector).label)
        assertEquals("Having trouble signing on?", (node.collectors[3] as FlowCollector).label)
        assertEquals("No account? Register now!", (node.collectors[4] as FlowCollector).label)

        // Fill in the login form with valid credentials and submit...
        (node.collectors[0] as? TextCollector)?.value = username
        (node.collectors[1] as? PasswordCollector)?.value = password
        (node.collectors[2] as? SubmitCollector)?.value = "Sign On"

        node = node.next()
        node = node as Connector

        assertTrue(node.collectors.size == 3)

        assertTrue(node.collectors[0] is SubmitCollector)
        assertTrue(node.collectors[1] is FlowCollector)
        assertTrue(node.collectors[2] is FlowCollector)

        assertEquals("Successful login", node.name)
        assertEquals("Successfully logged in to DaVinci", node.description)
        assertEquals("Continue", (node.collectors[0] as SubmitCollector).label)
        assertEquals("Reset password...", (node.collectors[1] as FlowCollector).label)
        assertEquals("Delete user...", (node.collectors[2] as FlowCollector).label)
        // Click continue
        (node.collectors[0] as? SubmitCollector)?.value = "Continue"

        node = node.next()
        assertTrue(node is Success)

        // Make sure the user is not null
        val user = node.user
        assertNotNull((user.token() as Result.Success).value.accessToken)

        val u = daVinci.user()
        u?.logout() ?: throw Exception("User is null")

        //After logout make sure the user is null
        assertNull(daVinci.user())
    }

    // C21275
    @Test
    fun loginFailure() = runTest {
        var node = daVinci.start() // Return first Node
        node = (node as Connector).next()
        node = node as Connector

        // Fill in the login form with invalid credentials and submit...
        (node.collectors[0] as? TextCollector)?.value = username
        (node.collectors[1] as? PasswordCollector)?.value = "invalid"
        (node.collectors[2] as? SubmitCollector)?.value = "Sign On"

        node = node.next()
        assertTrue(node is Failure)
        assertNotNull(node.input)
        assertEquals("Invalid username and/or password", node.message.trim())

        assertNull(daVinci.user())
    }

    // C21276
    @Test
    fun checkActiveSession() = runTest {
        var node = daVinci.start() // Return first Node
        assertTrue(node is Connector)
        node = (node as Connector)

        // Skip the first node
        node = node.next() as Connector

        // Fill in the login form with valid credentials and submit...
        (node.collectors[0] as? TextCollector)?.value = username
        (node.collectors[1] as? PasswordCollector)?.value = password
        (node.collectors[2] as? SubmitCollector)?.value = "Sign On"

        // Click on the "Continue" button to finish the login process
        node = node.next()
        node = node as Connector
        (node.collectors[0] as? SubmitCollector)?.value = "Continue"

        node = node.next()
        assertTrue(node is Success)

        // Make sure the user is not null
        val user = node.user
        assertNotNull((user.token() as Result.Success).value.accessToken)

        // Launch the login form again (active session exists...)
        // Should go directly to success...
        val node1 = daVinci.start()
        assertTrue(node1 is Success)

        // Logout the user
        val u = daVinci.user()
        u?.logout() ?: throw Exception("User is null")

        // After logout make sure the user is null
        assertNull(daVinci.user())
    }

    // C21253
    @Test
    fun userRegistrationSuccess() = runBlocking {
        var node = daVinci.start()
        node = (node as Connector).next() as Connector

        // Make sure that we are at the login form
        assertEquals("E2E Login Form", node.name)

        // Click on the registration link
        (node.collectors[4] as? FlowCollector)?.value = "register"
        node = node.next()

        assertTrue(node is Connector)

        // Validate the registration form
        assertEquals(6, node.collectors.size )
        assertEquals("Registration Form", node.name)
        assertEquals("Fill the form below to register a new account", node.description)
        assertEquals("First Name", (node.collectors[0] as TextCollector).label)
        assertEquals("Last Name", (node.collectors[1] as TextCollector).label)
        assertEquals("Email", (node.collectors[2] as TextCollector).label)
        assertEquals("Password", (node.collectors[3] as PasswordCollector).label)
        assertEquals("Save", (node.collectors[4] as SubmitCollector).label)
        assertEquals("Already have an account? Sign on", (node.collectors[5] as FlowCollector).label)

        // Fill in the registration form
        val newUser = "e2e" + System.currentTimeMillis() + "@example.com"
        (node.collectors[0] as? TextCollector)?.value = userFname
        (node.collectors[1] as? TextCollector)?.value = userLname
        (node.collectors[2] as? TextCollector)?.value = newUser
        (node.collectors[3] as? PasswordCollector)?.value = password
        (node.collectors[4] as? SubmitCollector)?.value = "Save"

        node = node.next() as Connector

        // User should be navigated to the verification code screen
        assertTrue(node.collectors.size == 3 )

        assertTrue(node.collectors[0] is TextCollector)
        assertTrue(node.collectors[1] is SubmitCollector)
        assertTrue(node.collectors[2] is FlowCollector)

        assertEquals("Enter verification code", node.name)
        assertEquals("Hint: The verification code is 1234", node.description)
        assertEquals("Verification Code", (node.collectors[0] as TextCollector).label)
        assertEquals("Verify", (node.collectors[1] as SubmitCollector).label)
        assertEquals("Resend Verification Code", (node.collectors[2] as FlowCollector).label)

        // Fill in the verification code and submit
        (node.collectors[0] as? TextCollector)?.value = verificationCode
        (node.collectors[1] as? SubmitCollector)?.value = "Verify"
        node = node.next()
        assertTrue(node is Connector)

        // User should be navigated to the "Successful user creation" screen...
        assertTrue(node.collectors.size == 1 )

        assertTrue(node.collectors[0] is SubmitCollector)
        assertEquals("Successful user creation", node.name)
        assertEquals("The PingOne user has been successfully created and verified", node.description)
        assertEquals("Continue", (node.collectors[0] as SubmitCollector).label)

        // Click "Continue" to finish the registration process
        (node.collectors[0] as? SubmitCollector)?.value = "Continue"
        node = node.next()
        assertTrue(node is Success)

        // Make sure the user is not null
        val user = node.user
        assertNotNull((user.token() as Result.Success).value.accessToken)

        val u = daVinci.user()
        u?.logout() ?: throw Exception("User is null")

        //After logout make sure the user is null
        assertNull(daVinci.user())

        // Delete the user from PingOne
        deleteUser(newUser, password)
    }

    // C21269
    @Test
    fun userRegistrationFailureUserAlreadyExists() = runTest {
        var node = daVinci.start()
        node = (node as Connector).next() as Connector

        // Make sure that we are at the login form
        assertEquals("E2E Login Form", node.name)

        // Click on the registration link
        (node.collectors[4] as? FlowCollector)?.value = "click"
        node = node.next() as Connector

        // Make sure that we are at the registration form
        assertEquals("Registration Form", node.name)

        // Fill in the registration form with user that already exists
        (node.collectors[0] as? TextCollector)?.value = userFname
        (node.collectors[1] as? TextCollector)?.value = userLname
        (node.collectors[2] as? TextCollector)?.value = username
        (node.collectors[3] as? PasswordCollector)?.value = password
        (node.collectors[4] as? SubmitCollector)?.value = "Save"

        val failureNode = node.next()
        assertTrue(failureNode is Failure)

        assertEquals("400", failureNode.input["code"].toString())
        assertEquals("uniquenessViolation username: is unique but a non-unique value is provided", failureNode.message.trim())

        // Make sure that we are still at the registration form
        assertEquals("Registration Form", node.name)
        assertNull(daVinci.user())
    }

    // C21270
    @Test
    fun userRegistrationFailureInvalidEmail() = runTest {
        var node = daVinci.start()
        node = (node as Connector).next() as Connector

        // Make sure that we are at the login form
        assertEquals("E2E Login Form", node.name)

        // Click on the registration link
        (node.collectors[4] as? FlowCollector)?.value = "click"
        node = node.next() as Connector

        // Make sure that we are at the registration form
        assertEquals("Registration Form", node.name)

        // Fill in the registration form with empty username (email)
        (node.collectors[0] as? TextCollector)?.value = userFname
        (node.collectors[1] as? TextCollector)?.value = userLname
        (node.collectors[2] as? TextCollector)?.value = ""
        (node.collectors[3] as? PasswordCollector)?.value = password
        (node.collectors[4] as? SubmitCollector)?.value = "Save"

        var failureNode = node.next()
        assertTrue(failureNode is Failure)

        assertEquals("400", failureNode.input["code"].toString())
        assertEquals("invalidInput \"username\" - must not be blank", failureNode.message.trim())

        // Make sure that we are still at the registration form
        assertEquals("Registration Form", node.name)

        // Fill in the registration form with empty username (email)
        (node.collectors[0] as? TextCollector)?.value = userFname
        (node.collectors[1] as? TextCollector)?.value = userLname
        (node.collectors[2] as? TextCollector)?.value = "invalid-email"
        (node.collectors[3] as? PasswordCollector)?.value = password
        (node.collectors[4] as? SubmitCollector)?.value = "Save"

        failureNode = node.next()
        assertTrue(failureNode is Failure)

        assertEquals("400", failureNode.input["code"].toString())
        assertEquals("invalidValue email: must be a well-formed email address", failureNode.message.trim())

        assertNull(daVinci.user())
    }

    // C21272
    @Test
    fun userRegistrationFailureInvalidPassword() = runTest {
        var node = daVinci.start()
        node = (node as Connector).next() as Connector

        // Make sure that we are at the login form
        assertEquals("E2E Login Form", node.name)

        // Click on the registration link
        (node.collectors[4] as? FlowCollector)?.value = "click"
        node = node.next() as Connector

        // Make sure that we are at the registration form
        assertEquals("Registration Form", node.name)

        // Fill in the registration form with user that already exists
        (node.collectors[0] as? TextCollector)?.value = userFname
        (node.collectors[1] as? TextCollector)?.value = userLname
        (node.collectors[2] as? TextCollector)?.value = userFname + System.currentTimeMillis() + "@example.com"
        (node.collectors[3] as? PasswordCollector)?.value = "invalid"
        (node.collectors[4] as? SubmitCollector)?.value = "Save"

        val failureNode = node.next()
        assertTrue(failureNode is Failure)

        assertEquals("400", failureNode.input["code"].toString())
        assertEquals("invalidValue password: User password did not satisfy password policy requirements", failureNode.message.trim())

        // Make sure that we are still at the registration form
        assertEquals("Registration Form", node.name)

        assertNull(daVinci.user())
    }

    // C21273
    @Test
    fun userRegistrationFailureInvalidVerificationCode() = runBlocking {
        var node = daVinci.start()
        node = (node as Connector).next()
        node = node as Connector

        // Make sure that we are at the login form
        assertEquals("E2E Login Form", node.name)

        // Click on the registration link
        (node.collectors[4] as? FlowCollector)?.value = "click"
        node = node.next() as Connector

        // Make sure that we are at the registration form
        assertEquals("Registration Form", node.name)

        // Fill in the registration form
        val newUser = userFname + System.currentTimeMillis() + "@example.com"
        (node.collectors[0] as? TextCollector)?.value = userFname
        (node.collectors[1] as? TextCollector)?.value = userLname
        (node.collectors[2] as? TextCollector)?.value = newUser
        (node.collectors[3] as? PasswordCollector)?.value = password
        (node.collectors[4] as? SubmitCollector)?.value = "Save"

        node = node.next() as Connector

        // Make sure that we are at the "Verification Code" screen
        assertEquals("Enter verification code", node.name)

        // Enter invalid verification code and submit
        (node.collectors[0] as? TextCollector)?.value = "invalid"
        (node.collectors[1] as? SubmitCollector)?.value = "Verify"
        val failureNode = node.next()
        assertTrue(failureNode is Failure)

        assertEquals("400", failureNode.input["code"].toString())
        assertEquals("Invalid verification code", failureNode.message.trim())

        // Make sure that we are still at the registration form
        assertEquals("Enter verification code", node.name)

        // Resend the verification code
        (node.collectors[2] as? FlowCollector)?.value = "click"
        node = node.next() as Connector

        // Make sure that we are still at the registration form
        assertEquals("Enter verification code", node.name)

        assertNull(daVinci.user())
        deleteUser(newUser, password)
    }

    // C21277
    @Test
    fun passwordRecovery() = runBlocking {
        val newUser = userFname + System.currentTimeMillis() + "@example.com"

        // Register a user first...
        var node = daVinci.start()
        node = (node as Connector).next()
        node = node as Connector

        // Make sure that we are at the login form
        assertEquals("E2E Login Form", node.name)

        // Click on the registration link
        (node.collectors[4] as? FlowCollector)?.value = "click"
        node = node.next() as Connector

        // Make sure that we are at the registration form
        assertEquals("Registration Form", node.name)

        // Fill in the registration form
        (node.collectors[0] as? TextCollector)?.value = userFname
        (node.collectors[1] as? TextCollector)?.value = userLname
        (node.collectors[2] as? TextCollector)?.value = newUser
        (node.collectors[3] as? PasswordCollector)?.value = password
        (node.collectors[4] as? SubmitCollector)?.value = "Save"

        node = node.next() as Connector

        // Make sure that we are at the "Verification Code" screen
        assertEquals("Enter verification code", node.name)

        // Enter verification code and submit
        (node.collectors[0] as? TextCollector)?.value = "1234"
        (node.collectors[1] as? SubmitCollector)?.value = "Verify"
        node = node.next()
        assertTrue(node is Connector)

        // User should be navigated to the "Successful user creation" screen...
        assertEquals("Successful user creation", node.name)

        // Click "Continue" to finish the registration process
        (node.collectors[0] as? SubmitCollector)?.value = "Continue"
        node = node.next()
        assertTrue(node is Success)

        // Make sure the user is not null
        var user = node.user
        assertNotNull((user.token() as Result.Success).value.accessToken)

        // Logout the user
        var u = daVinci.user()
        u?.logout() ?: throw Exception("User is null")

        /// Login again...
        node = daVinci.start()
        node = (node as Connector).next() as Connector

        // Click on the "Having trouble..." link
        (node.collectors[3] as? FlowCollector)?.value = "click"
        node = node.next() as Connector

        // At the "Forgot password" screen...
        assertTrue(node.collectors.size == 3)

        assertTrue(node.collectors[0] is TextCollector)
        assertTrue(node.collectors[1] is SubmitCollector)
        assertTrue(node.collectors[2] is FlowCollector)

        assertEquals("Forgot password", node.name)
        assertEquals("Submit username to reset your password", node.description)
        assertEquals("Username", (node.collectors[0] as TextCollector).label)
        assertEquals("Submit", (node.collectors[1] as SubmitCollector).label)
        assertEquals("Cancel", (node.collectors[2] as FlowCollector).label)

        // Fill in the username and submit
        (node.collectors[0] as? TextCollector)?.value = newUser
        (node.collectors[1] as? SubmitCollector)?.value = "Submit"
        node = node.next()
        assertTrue(node is Connector)

        // At the "Forgot password recovery code" screen...
        assertTrue(node.collectors.size == 4)

        assertTrue(node.collectors[0] is TextCollector)
        assertTrue(node.collectors[1] is PasswordCollector)
        assertTrue(node.collectors[2] is SubmitCollector)
        assertTrue(node.collectors[3] is FlowCollector)

        assertEquals("Forgot password recovery code", node.name)
        assertEquals(
            "Enter recovery code and a new password. Hint: Recovery code is 1234.",
            node.description
        )
        assertEquals("Recovery Code", (node.collectors[0] as TextCollector).label)
        assertEquals("New Password", (node.collectors[1] as PasswordCollector).label)
        assertEquals("Submit", (node.collectors[2] as SubmitCollector).label)
        assertEquals("Cancel", (node.collectors[3] as FlowCollector).label)

        // Fill in the recovery code and password and submit
        (node.collectors[0] as? TextCollector)?.value = verificationCode
        (node.collectors[1] as? PasswordCollector)?.value = "New$password"
        (node.collectors[2] as? SubmitCollector)?.value = "Submit"
        node = node.next()
        assertTrue(node is Connector)

        // At the "Successful password reset" screen...
        assertTrue(node.collectors.size == 1)
        assertEquals("Successful password reset", node.name)
        assertEquals("Password was successfully reset", node.description)
        assertEquals("Continue", (node.collectors[0] as SubmitCollector).label)

        // Click "Continue" to finish the password reset process
        (node.collectors[0] as? SubmitCollector)?.value = "Continue"
        node = node.next()
        assertTrue(node is Success)

        // Make sure the user is not null
        user = node.user
        assertNotNull((user.token() as Result.Success).value.accessToken)

        u = daVinci.user()
        u?.logout() ?: throw Exception("User is null")

        // After logout make sure the user is null
        assertNull(daVinci.user())

        // Delete the user from PingOne
        deleteUser(newUser, "New$password")
    }

    // C21278
    @Test
    fun passwordReset() = runBlocking {
        val newUser = userFname + System.currentTimeMillis() + "@example.com"

        // Register a user first...
        var node = daVinci.start()
        node = (node as Connector).next()
        node = node as Connector

        // Make sure that we are at the login form
        assertEquals("E2E Login Form", node.name)

        // Click on the registration link
        (node.collectors[4] as? FlowCollector)?.value = "click"
        node = node.next() as Connector

        // Make sure that we are at the registration form
        assertEquals("Registration Form", node.name)

        // Fill in the registration form
        (node.collectors[0] as? TextCollector)?.value = userFname
        (node.collectors[1] as? TextCollector)?.value = userLname
        (node.collectors[2] as? TextCollector)?.value = newUser
        (node.collectors[3] as? PasswordCollector)?.value = password
        (node.collectors[4] as? SubmitCollector)?.value = "Save"

        node = node.next() as Connector

        // Make sure that we are at the "Verification Code" screen
        assertEquals("Enter verification code", node.name)

        // Enter verification code and submit
        (node.collectors[0] as? TextCollector)?.value = "1234"
        (node.collectors[1] as? SubmitCollector)?.value = "Verify"
        node = node.next()
        assertTrue(node is Connector)

        // User should be navigated to the "Successful user creation" screen...
        assertEquals("Successful user creation", node.name)

        // Click "Continue" to finish the registration process
        (node.collectors[0] as? SubmitCollector)?.value = "Continue"
        node = node.next()
        assertTrue(node is Success)

        // Make sure the user is not null
        var user = node.user
        assertNotNull((user.token() as Result.Success).value.accessToken)

        // Logout the user
        var u = daVinci.user()
        u?.logout() ?: throw Exception("User is null")

        /// Login again...
        node = daVinci.start()
        node = (node as Connector).next() as Connector

        // Make suer that we are at the Login form...
        assertEquals("E2E Login Form", node.name)

        // Fill in the login form with valid credentials and submit...
        (node.collectors[0] as? TextCollector)?.value = newUser
        (node.collectors[1] as? PasswordCollector)?.value = password
        (node.collectors[2] as? SubmitCollector)?.value = "Sign On"

        node = node.next() as Connector

        // Make sure that we are at the "Successful login" screen...
        assertEquals("Successful login", node.name)

        // Click reset password link
        (node.collectors[1] as? FlowCollector)?.value = "Reset Password"
        node = node.next() as Connector

        // Make sure that we are at the "Reset Password" screen...
        assertEquals("Reset Password", node.name)

        // Fill in the reset password form with the same password
        (node.collectors[0] as? PasswordCollector)?.value = password
        (node.collectors[1] as? PasswordCollector)?.value = password
        (node.collectors[2] as? SubmitCollector)?.value = "Save"

        var failureNode = node.next()
        assertTrue(failureNode is Failure)

        assertEquals("400", failureNode.input["code"].toString())
        assertEquals("newPasswordNotValid newPassword: New password did not satisfy password policy requirements", failureNode.message.trim())

        // Make sure that we are still at the "Reset Password" screen
        assertEquals("Reset Password", node.name)

        // Fill in the reset password form with the weak new password
        (node.collectors[0] as? PasswordCollector)?.value = password
        (node.collectors[1] as? PasswordCollector)?.value = "weak"
        (node.collectors[2] as? SubmitCollector)?.value = "Save"

        failureNode = node.next()
        assertTrue(failureNode is Failure)

        assertEquals("400", failureNode.input["code"].toString())
        assertEquals("newPasswordNotValid newPassword: New password did not satisfy password policy requirements", failureNode.message.trim())

        // Fill in the reset password form with valid new password
        (node.collectors[0] as? PasswordCollector)?.value = password
        (node.collectors[1] as? PasswordCollector)?.value = "New" + password
        (node.collectors[2] as? SubmitCollector)?.value = "Save"
        node = node.next() as Connector

        assertEquals("Reset password success", node.name)
        assertEquals("Password has been successfully reset", node.description)
        assertEquals("Continue", (node.collectors[0] as SubmitCollector).label)
        (node.collectors[0] as? SubmitCollector)?.value = "Continue"
        node = node.next()

        assertTrue(node is Success)

        // Make sure the user is not null
        user = node.user
        assertNotNull((user.token() as Result.Success).value.accessToken)

        // Logout the user
        u = daVinci.user()
        u?.logout() ?: throw Exception("User is null")

        // After logout make sure the user is null
        assertNull(daVinci.user())

        // Delete the user from PingOne
        deleteUser(newUser, "New$password")
    }

    private suspend fun deleteUser(username: String, password: String) {
        var node = daVinci.start()
        node = (node as Connector).next() as Connector

        // Make sure that we are at the Login form...
        assertEquals("E2E Login Form", node.name)

        // Fill in the login form with valid credentials and submit...
        (node.collectors[0] as? TextCollector)?.value = username
        (node.collectors[1] as? PasswordCollector)?.value = password
        (node.collectors[2] as? SubmitCollector)?.value = "Sign On"

        node = node.next() as Connector

        // Make sure that we are at the "Successful login" screen...
        assertEquals("Successful login", node.name)

        // Click delete user button
        (node.collectors[2] as? FlowCollector)?.value = "Delete User"
        node = node.next() as Connector


        // Validate success user deletion screen
        assertEquals("Success", node.name)
        assertEquals("User has been successfully deleted", node.description)
    }
}