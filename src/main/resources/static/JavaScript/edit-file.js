document.addEventListener("DOMContentLoaded", function () {
    const fileId = window.location.pathname.split("/").pop();
    const username = document.getElementById("username").textContent.trim();
    const contentArea = document.getElementById("content");
    const logOutput = document.getElementById("log");
    let previousContent = "";
    const codeCache = new Map();
    let typingTimer; // Timer for debouncing
    const debounceDelay = 100;

    // Load code from the server when the page loads
    fetch(`/api/files/${fileId}`)
        .then(response => response.json())
        .then(data => {
            contentArea.value = data.content;
            previousContent = data.content;
        })
        .catch(error => console.error('Error loading code:', error));

    // Set up WebSocket connection using SockJS
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    // Connect to the WebSocket
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/code/' + fileId, function (message) {
            const messageBody = message.body; // Get the message body directly

            // Attempt to parse the incoming message
            let changeRequest;

            try {
                // Parse the ChangeRequest object from the incoming message
                changeRequest = JSON.parse(messageBody);
            } catch (e) {
                // If parsing fails, treat it as current code
                contentArea.value = messageBody; // Treat as new code content
                previousContent = contentArea.value; // Update previous content
                return; // Exit early since we can't process further
            }

            // Apply changes based on ChangeRequest properties
            if (changeRequest.updatedBy !== username) {
                const currentContent = contentArea.value;

                if (changeRequest.text) {
                    contentArea.value = currentContent.slice(0, changeRequest.start) +
                        changeRequest.text +
                        currentContent.slice(changeRequest.end);
                } else {
                    // If text is empty, it means a deletion
                    contentArea.value = currentContent.slice(0, changeRequest.start) +
                        currentContent.slice(changeRequest.end);
                }

                previousContent = contentArea.value; // Update previous content
            }
        });

        // Subscribe to the /topic/join/{fileId} channel for join notifications specific to this file
        stompClient.subscribe(`/topic/join/${fileId}`, (message) => {
            logOutput.textContent += `${message.body}\n`; // Log join messages specific to this file
        });



        // Subscribe to the /topic/refresh/{fileId} channel for refresh notifications specific to this file
        stompClient.subscribe(`/topic/refresh/${fileId}`, function (message) {
            alert("The content for this file has been updated by another user. Refreshing the page.");
            window.location.reload(); // Refresh the page
        });

    });

    // Listen for changes in the code editor
    contentArea.addEventListener("input", function () {
        const currentContent = contentArea.value;

        // Find the difference between previous content and current content
        const diff = findDifference(previousContent, currentContent);

        if (diff) {
            clearTimeout(typingTimer); // Clear the previous timer
            typingTimer = setTimeout(() => {
                // Prepare the update object to send to the server
                const update = {
                    fileId: fileId,
                    start: diff.start,
                    end: diff.end,
                    text: diff.text,
                    updatedBy: username,
                    timestamp: new Date().toISOString(),
                };

                // Send the change details to the server
                stompClient.send("/app/code-edit", {}, JSON.stringify(update));

                previousContent = currentContent; // Update previous content
            }, debounceDelay);
        }
    });

    contentArea.addEventListener("blur", function () {
        const currentContent = contentArea.value;

        if (previousContent !== currentContent) {
            const update = {
                fileId: fileId,
                start: 0,
                end: currentContent.length,
                text: currentContent,
                updatedBy: username,
                timestamp: new Date().toISOString()
            };

            stompClient.send("/app/code-edit", {}, JSON.stringify(update));
            previousContent = currentContent;
        }
    });

    // Function to find the difference between two strings (manual diff)
    function findDifference(oldText, newText) {
        let start = 0;

        while (start < oldText.length && start < newText.length && oldText[start] === newText[start]) {
            start++;
        }

        let oldEnd = oldText.length - 1;
        let newEnd = newText.length - 1;
        while (oldEnd >= start && newEnd >= start && oldText[oldEnd] === newText[newEnd]) {
            oldEnd--;
            newEnd--;
        }

        if (start === oldText.length && start === newText.length) {
            return null; // No change
        }

        const text = newText.slice(start, newEnd + 1);
        return {
            start: start,
            end: oldEnd + 1,
            text: text
        };
    }

    // Load Dashboard to Cache button functionality
    const loadDashboardBtn = document.getElementById("loadDashboardBtn");
    if (loadDashboardBtn) {
        loadDashboardBtn.onclick = function () {
            fetch(`/api/files/${fileId}/dashboard`) // Adjust this endpoint as necessary
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(dashboard => {
                    codeCache.set(dashboard.fileId.toString(), new String(dashboard.content));

                    contentArea.value = dashboard.content;
                    previousContent = dashboard.content;

                    // Notify all users to refresh their pages
                    stompClient.send("/app/refresh", {}, JSON.stringify({ message: "Dashboard loaded, please refresh." }));

                    alert("Dashboard loaded into cache successfully!");
                })
                .catch(error => {
                    console.error('Error loading dashboard:', error);
                    alert('Failed to load dashboard into cache: ' + error.message);
                });
        };
    }

    // Revert file to a specific version and load content into cache
    window.revertFile = function(fileId, versionId) {
        fetch(`/api/files/${fileId}/revert/${versionId}`, {
            method: 'POST'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error reverting file: ' + response.status + ' ' + response.statusText);
                }
                return response.json();
            })
            .then(revertedFile => {
                codeCache.set(revertedFile.id.toString(), new String(revertedFile.content));

                contentArea.value = revertedFile.content;
                previousContent = revertedFile.content;

                // Notify all users to refresh their pages
                stompClient.send("/app/refresh", {}, JSON.stringify({ message: "File reverted, please refresh." }));

                alert('File reverted successfully!');
            })
            .catch(error => {
                console.error('Error reverting file:', error);
                alert('Error reverting file: ' + error.message);
            });
    };
});
