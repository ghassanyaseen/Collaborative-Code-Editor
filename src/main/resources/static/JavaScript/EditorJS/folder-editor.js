document.addEventListener('DOMContentLoaded', function() {
    const folderId = window.location.pathname.split("/").pop();  // Get the folder ID from the URL

    // Create new folder inside the current folder
    document.getElementById('createFolderInsideButton').addEventListener('click', function() {
        const folderName = document.getElementById('newFolderName').value;

        if (!folderName) {
            alert('Please enter a folder name.');
            return;
        }

        fetch(`/api/folders/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
                folderName: folderName,
                parentFolderId: folderId,
                branchId: getBranchId()
            })
        })
            .then(response => {
                if (response.ok) {
                    return response.text().then(text => {
                        alert(text);
                        loadFolderDetails();
                    });
                } else {
                    return response.text().then(text => {
                        alert('Error creating folder: ' + text);
                    });
                }
            })
            .catch(error => console.error('Error creating folder:', error));
    });

    // Create new file inside the current folder
    document.getElementById('createFileInsideButton').addEventListener('click', function() {
        const fileName = document.getElementById('newFileName').value;
        const languageType = document.getElementById('languageType').value;
        const fullFileName = `${fileName}`;

        if (!fileName) {
            alert('Please enter a file name.');
            return;
        }

        fetch(`/api/files/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
                fileName: fullFileName,
                content: '',
                languageType: languageType,
                parentFolderId: folderId,
                branchId: getBranchId(),
            })
        })
            .then(response => {
                if (response.ok) {
                    alert('File created successfully!');
                    loadFolderDetails();
                } else {
                    return response.text().then(text => {
                        alert('Error creating file: ' + text);
                    });
                }
            })
            .catch(error => console.error('Error creating file:', error));
    });

    // Function to retrieve branchId
    function getBranchId() {
        return 1;
    }

    // Load folder details
    function loadFolderDetails() {
        fetch(`/api/folders/${folderId}`)
            .then(response => response.json())
            .then(folder => {
                document.getElementById('folderName').textContent = folder.folderName;
                document.getElementById('createdBy').textContent = folder.createdBy;


                loadSubfolders(folder.subFolders || []);
                loadFiles(folder.files || []);
            })
            .catch(error => console.error('Error fetching folder details:', error));
    }

    // Load subfolders into a table
    function loadSubfolders(subfolders) {
        const subfolderList = document.getElementById('subfolderList').getElementsByTagName('tbody')[0];
        subfolderList.innerHTML = '';

        if (subfolders.length === 0) {
            subfolderList.innerHTML = '<tr><td colspan="2">No subfolders found</td></tr>';
        } else {
            subfolders.forEach(subfolder => {
                const row = document.createElement('tr');
                row.innerHTML = `
                <td>
                    <button class="folder-button" onclick="location.href='/editor/folder-editor/${subfolder.id}'">${subfolder.folderName}</button>
                </td>
                <td>
                    <button class="delete-button deleteFolderButton" data-folder-id="${subfolder.id}">Delete</button>
                </td>
            `;
                subfolderList.appendChild(row);
            });

            // Attach delete functionality to each button
            document.querySelectorAll('.deleteFolderButton').forEach(button => {
                button.onclick = function() {
                    const subfolderId = this.getAttribute('data-folder-id');
                    deleteFolder(subfolderId);
                };
            });
        }
    }

    // Delete folder function
    function deleteFolder(folderId) {
        if (!confirm('Are you sure you want to delete this folder and all its contents?')) {
            return;
        }

        fetch(`/api/folders/${folderId}`, {
            method: 'DELETE',
        })
            .then(response => {
                if (response.ok) {
                    alert('Folder deleted successfully!');
                    loadFolderDetails();
                } else {
                    return response.text().then(text => {
                        alert('Error deleting folder: ' + text);
                    });
                }
            })
            .catch(error => {
                console.error('Error deleting folder:', error);
                alert('An unexpected error occurred while deleting the folder: ' + error.message);
            });
    }

    // Load files into a table
    function loadFiles(files) {
        const fileList = document.getElementById('fileList').getElementsByTagName('tbody')[0];
        fileList.innerHTML = '';

        if (files.length === 0) {
            fileList.innerHTML = '<tr><td colspan="4">No files found</td></tr>';
        } else {
            files.forEach(file => {
                const row = document.createElement('tr');
                row.innerHTML = `
                <td>
                    <button class="file-button" onclick="location.href='/editor/file-editor/${file.id}'">${file.fileName}</button>
                </td>
                <td>${file.languageType}</td>
                <td>${file.createdBy || 'Unknown'}</td>
                <td>
                    <button class="delete-button deleteFileButton" data-file-id="${file.id}">Delete</button>
                    <button class="download-button" onclick="downloadFile('${file.fileName}')">Download</button>
                </td>
            `;
                fileList.appendChild(row);
            });

            // Attach delete functionality to each button
            document.querySelectorAll('.deleteFileButton').forEach(button => {
                button.onclick = function() {
                    const fileId = this.getAttribute('data-file-id');
                    deleteFile(fileId);
                };
            });
        }
    }

    // Delete file function
    function deleteFile(fileId) {
        if (!confirm('Are you sure you want to delete this file?')) {
            return;
        }

        fetch(`/api/files/delete/${fileId}`, {
            method: 'DELETE',
        })
            .then(response => {
                if (response.ok) {
                    alert('File deleted successfully!');
                    loadFolderDetails();
                } else {
                    alert('Error deleting file');
                }
            })
            .catch(error => console.error('Error deleting file:', error));
    }

    // Load folder details on page load
    loadFolderDetails();
});
