document.addEventListener('DOMContentLoaded', function() {
    const folderId = window.location.pathname.split("/").pop();  // Get the folder ID from the URL

    // Load folder details
    loadFolderDetails();

    // Load folder details function
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

    function loadSubfolders(subfolders) {
        const subfolderList = document.getElementById('subfolderList').getElementsByTagName('tbody')[0];
        subfolderList.innerHTML = '';

        if (subfolders.length === 0) {
            subfolderList.innerHTML = '<tr><td colspan="1">No subfolders found</td></tr>';
        } else {
            subfolders.forEach(subfolder => {
                const row = document.createElement('tr');
                row.innerHTML = `
                <td>
                    <button class="folder-button" onclick="location.href='/viewer/folder-viewer/${subfolder.id}'">${subfolder.folderName}</button>
                </td>
            `;
                subfolderList.appendChild(row);
            });
        }
    }

    // Load files into a table
    function loadFiles(files) {
        const fileList = document.getElementById('fileList').getElementsByTagName('tbody')[0];
        fileList.innerHTML = '';

        if (files.length === 0) {
            fileList.innerHTML = '<tr><td colspan="3">No files found</td></tr>';
        } else {
            files.forEach(file => {
                const row = document.createElement('tr');
                row.innerHTML = `
                <td>
                    <button class="file-button" onclick="location.href='/viewer/file-viewer/${file.id}'">${file.fileName}</button>
                </td>
                <td>${file.languageType}</td>
                <td>${file.createdBy || 'Unknown'}</td>
            `;
                fileList.appendChild(row);
            });
        }
    }
});
