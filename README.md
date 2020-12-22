# Shatter
Low-effort hybrid authentication for [Velocity](https://github.com/VelocityPowered/Velocity), a Minecraft server proxy.

**Note: Shatter requires connecting to an implementation of the Shatter API to function.**

## Introduction

### What?
Shatter implements an alternative host-key-based authentication method for clients that cannot access the Internet or Mojang's authentication servers while the Velocity proxy operates in online mode.

#### How it works
1. A ShatterPlayer is created on a ShatterAPI implementation, containing the following:
    - **Login ID**: Offline mode username to be entered into a launcher such as [MultiMC](https://github.com/MultiMC/MultiMC5).
    - **Name**: The username that the Minecraft servers and other players see. (**Cannot be a username that's already taken by a Mojang account.**)
    - **Host Key**: The server address that the user connects with. (**Wildcard DNS must be implemented correctly with your nameserver.**)
2. When a player connects, the plugin queries the ShatterAPI for a user with matching Login ID and Host Key.
3. If a matching player is found, the connection is forced into offline mode, allowing the player to connect.
4. The player appears as a regular player in the server, but with a type 3 UUID typically used for offline mode.

### Why?
There are many use cases for Shatter, of which two notable ones are mentioned here:

1. **Tournaments**: In local tournaments, organisers can restrict access to the network beyond the Minecraft server for participants without the issues associated with offline mode servers, such as account impersonation or having to use password authentication plugins. Players who can authenticate with Mojang (for example, spectators) are completely unaffected by Shatter.

2. **Server Administration**: For server owners and admins, Shatter allows the creation of unlimited alternative accounts for testing and moderation (for example, only having admin powers on a separate account), without disrupting gameplay on their main accounts.

## Implementing ShatterAPI
ShatterAPI only requires one endpoint to be implemented, but there are many details that must be kept in mind. They are explained below.

1. The API must be configured to use HTTPS (specifically, over TLS).
    - If using a self-signed certificate, the cert file must be added to the plugin folder and the config file.
2. The login ID **should not change**.
    - The Minecraft UUID is derived from the login ID, and player data will be lost if changed.
    - Ideally, the login ID should also be a username that is normally unobtainable, to prevent issues for Mojang-authenticated users.
      - You can do this by adding symbols such as `.` or `*`.
3. The player's name must be an available username.
    - The plugin checks for username uniqueness upon login, and using an unavailable username would prevent the player from joining.
    - The API should prevent users from registering names that Mojang marks as unavailable (is linked to a UUID).
    - If the username is taken later on, it should allow the player to change their name (player data will not be affected).
4. (Optional) Using host keys with Forge.
    - Forge appends `FML` or `FML2` to the end of the host string.
    - You may want to check and remove this if you'd like to support Forge clients.

### POST `/authenticate`
Authentication request by the plugin.

#### Request JSON Body
| Key | Type | Description |
| - | - | - |
| `loginId` | String | Login ID |
| `hostKey` | String | Host Key |

#### Response (player doesn't exist, or host key doesn't match)
**Status Code**: 204 (NO CONTENT)
**Return**: *n/a*

### Response (player exists, host key is valid)
**Status Code**: 200
**Status Code**: 200 (OK)
**Return**: A ShatterPlayer JSON.
| Key | Type | Description |
| - | - | - |
| `loginId` | String | Login ID |
| `name` | String | Player Name |
