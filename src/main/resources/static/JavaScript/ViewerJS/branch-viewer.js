// Load branches
function loadBranches() {
    fetch('/api/branches')
        .then(response => response.json())
        .then(branches => {
            const branchList = document.getElementById('branchList');
            branchList.innerHTML = '';

            branches.forEach(branch => {
                const row = document.createElement('tr');
                row.innerHTML = `
                        <td>
                            <button class="branchButton" data-branch-id="${branch.id}">${branch.branchName}</button>
                        </td>
                        <td>${branch.createdBy}</td>
                    `;
                branchList.appendChild(row);
            });

            document.querySelectorAll('.branchButton').forEach(button => {
                button.onclick = function() {
                    const branchId = this.getAttribute('data-branch-id');
                    viewBranchDetails(branchId);
                };
            });
        })
        .catch(error => console.error('Error loading branches:', error));
}

// Function to view branch details
function viewBranchDetails(branchId) {
    // Redirect to the branch detail page
    window.location.href = `/viewer/branch-detail-viewer/${branchId}`;
}

// Load branches on page load
document.addEventListener('DOMContentLoaded', loadBranches);