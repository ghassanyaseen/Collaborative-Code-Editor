document.addEventListener("DOMContentLoaded", function() {
    const fileId = window.location.pathname.split("/").pop(); // Get the last part of the URL

    // Fetch file details
    fetch(`/api/files/${fileId}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById("fileNameDisplay").textContent = data.fileName;
            document.getElementById("content").value = data.content;
        })
        .catch(error => console.error('Error fetching file:', error));

    // Fetch version history
    fetch(`/api/files/${fileId}/versions`)
        .then(response => response.json())
        .then(versions => {
            const versionList = document.getElementById('versionList');
            versions.forEach(version => {
                const listItem = document.createElement('li');
                listItem.innerHTML =
                    `Version ${version.versionNumber} - Updated at ${version.timestamp}
                    <button onclick="revertFile(${fileId}, ${version.id})">Revert</button>
                    <button onclick="viewVersion(${version.id})">View Details</button>
                    <button onclick="deleteVersion(${fileId}, ${version.id})">Delete Version</button>`;
                versionList.appendChild(listItem);
            });
        })
        .catch(error => console.error('Error fetching versions:', error));

    // Fetch comments
    fetch(`/api/files/${fileId}/comments`)
        .then(response => response.json())
        .then(comments => {
            const commentList = document.getElementById('commentList');
            comments.forEach(comment => {
                const listItem = document.createElement('li');
                listItem.textContent = `Line ${comment.lineNumber}: ${comment.content} (by ${comment.createdBy})`;
                commentList.appendChild(listItem);
            });
        })
        .catch(error => console.error('Error fetching comments:', error));

    // Add comment functionality
    const addCommentButton = document.getElementById("addCommentButton");
    if (addCommentButton) {
        addCommentButton.onclick = function() {
            const lineNumber = document.getElementById("lineNumber").value;
            const commentContent = document.getElementById("commentContent").value;

            fetch(`/api/files/${fileId}/comments`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({ lineNumber: lineNumber, content: commentContent })
            })
                .then(response => {
                    if (response.ok) {
                        alert('Comment added successfully!');
                        window.location.reload();
                    } else {
                        return response.text().then(text => {
                            alert('Error adding comment: ' + text);
                        });
                    }
                })
                .catch(error => console.error('Error adding comment:', error));
        };
    }

    // Revert file to a specific version
    window.revertFile = function(fileId, versionId) {
        fetch(`/api/files/${fileId}/revert/${versionId}`, {
            method: 'POST'
        })
            .then(response => {
                if (response.ok) {
                    alert('File reverted successfully!');
                } else {
                    alert('Error reverting file.');
                }
            })
            .catch(error => console.error('Error reverting file:', error));
    };

    // Update file functionality
    const updateButton = document.getElementById("updateButton");
    if (updateButton) {
        updateButton.onclick = function() {
            const updatedFileName = document.getElementById("fileNameDisplay").textContent; // Use textContent
            const updatedContent = document.getElementById("content").value;

            fetch(`/api/files/${fileId}/edit`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({ fileName: updatedFileName, content: updatedContent })
            })
                .then(response => {
                    if (response.ok) {
                        alert('File updated successfully!');
                        window.location.reload();
                    } else {
                        alert('Error updating file.');
                    }
                })
                .catch(error => console.error('Error updating file:', error));
        };
    }

    // Delete file functionality
    const deleteButton = document.getElementById("deleteButton");
    if (deleteButton) {
        deleteButton.onclick = function() {
            fetch(`/api/files/delete/${fileId}`, {
                method: 'DELETE'
            })
                .then(response => {
                    if (response.ok) {
                        alert('File deleted successfully!');
                        window.location.href = '/view-files';
                    } else {
                        alert('Error deleting file.');
                    }
                })
                .catch(error => console.error('Error deleting file:', error));
        };
    }

    // View a specific version's details
    window.viewVersion = function(versionId) {
        fetch(`/api/files/version/${versionId}`)
            .then(response => response.json())
            .then(version => {
                alert(`Version ID: ${version.id}\nContent: ${version.content}`);
            })
            .catch(error => console.error('Error fetching version details:', error));
    };

    // Delete a specific version
    window.deleteVersion = function(fileId, versionId) {
        fetch(`/api/files/${fileId}/versions/${versionId}`, {
            method: 'DELETE'
        })
            .then(response => {
                if (response.ok) {
                    alert('Version deleted successfully!');
                    window.location.reload();
                } else {
                    alert('Error deleting version.');
                }
            })
            .catch(error => console.error('Error deleting version:', error));
    };
});
