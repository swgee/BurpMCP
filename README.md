# BurpMCP

![Burp and LLMs unite](assets/cover_image.png)

BurpMCP is a Burp Suite extension to augment application security testers, vulnerability researchers, and bug bounty hunters with the capabilities of modern AI. Every day, large language models are gaining larger context windows, faster response times, and greater knowledge and reasoning skills. BurpMCP lets you take advantage of this inevitable technology while testing HTTP attack surfaces, providing a super-intelligent sidekick to help navigate unfamiliar attack surfaces and chase down complex vulnerabiltiies.

While other MCP servers for Burp Suite exist, they only provide generic access to Burp Suite tools and data, such as running scans, viewing issues, and reading proxy history. BurpMCP focuses on enhancing manual testing with the help of LLMs, implementing a clean user interface to easily provide AI with requests as context, monitor requests sent by the AI, and manage the MCP server. Additionally, both HTTP 1.1 and 2 are supported and tested for reliability.

## Installation

To install, simply download the jar file from the releases and load it into Burp.

The MCP server runs by default on localhost port 8181 over SSE. The configuration syntax varies depending on your MCP client, but here is an example using the [Dive](https://github.com/OpenAgentPlatform/Dive) MCP client:

```json
{
  "mcpServers": {
    "burp-mcp-server": {
      "enabled": true,
      "transport": "sse",
      "url": "http://localhost:8181/mcp/sse",
      "disabled": true
    }
  }
}
```

To use BurpMCP with Claude Desktop, download the `stdio-bridge.py` script and add the following configuration to your `claude_desktop_config.json`. Make sure the host and port match what you configure in BurpMCP.

```json
{
  "mcpServers": {
    "burp-mcp": {
      "command": "python3",
      "args": ["path/to/stdio-bridge.py", "http://localhost:8181/mcp/sse"],
      "env": {}
    }
  }
}
```

## Usage

To send requests to BurpMCP, right click any reques and click `Extensions -> Send to BurpMCP".

![Send a request to BurpMCP](assets/send_to_burpmcp.png)

The request will then be visible in the "Saved Requests" tab in BurpMCP. Each Saved Requests includes a Notes column to provide LLMs with additional context. LLMs can also update the notes in each saved request to keep track of important details or save requests for future analysis.

![Saved requests tab](assets/saved_requests.png)

Using your favorite MCP Client, you can prompt the LLM to retrieve saved requests by referencing their ID and send follow up requests to aid testing.

![Retrieve the saved request](assets/retrieve_saved_request.png)

Claude attempting to solve the lab:

![Claude attempting to solve the lab](assets/autonomous_testing.png)

## Examples

For some examples of using the extension successfully, check out the [examples](examples).

Special thanks to the following for providing some examples:

- @hunterverhelst

## Common Issues

- The LLMs sometimes forget to add important components to requests like Content-Length headers or URL encoding. You may need to let the LLM know if it is forgetting something and cannot resolve the issue on its own.

## Tool Definitions

The Tool specifications can be found in [src/main/java/burpmcp/tools](src/main/java/burpmcp/tools) and describe exactly what data the MCP server expects from the LLMs. This information is important to understand so you know what the LLMs are sending. You can modify the property descriptions depending on your use case which would require rebuilding the extension.

## Building from Source

```bash
git clone https://github.com/swgee/burpmcp.git
cd burpmcp
mvn clean package
```

## Contributing

For issues, feature requests, or questions, please open an issue. If you would like to contribute, submit a PR. If you have used the tool to successfully find vulnerabilities or anything else interesting, please send screenshots or chat logs and I would love to include them in the examples. Thank you for trying out BurpMCP and happy hacking!