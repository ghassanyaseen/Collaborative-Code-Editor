document.addEventListener("DOMContentLoaded", function() {
    const fileId = window.location.pathname.split("/").pop();
    const runButton = document.getElementById("runButton");
    const outputArea = document.getElementById("output");
    const contentArea = document.getElementById("content");
    const languageType = document.getElementById("languageType").textContent.trim();
    const branchId = document.getElementById("branchId").value;


    // Add event listener to the run button
    runButton.addEventListener("click", async function() {
        const codeContent = contentArea.value.trim();

        if (!codeContent) {
            outputArea.textContent = "Error: Code cannot be empty.";
            return; // Exit the function early
        }

        const executionRequest = {
            fileId: fileId,
            code: codeContent,
            language: languageType,
            branchId: branchId
        };


        try {

            const response = await fetch('/execute', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(executionRequest)
            });

            if (!response.ok) {
                const errorMessage = await response.text();
                throw new Error(`Execution failed: ${response.status} ${errorMessage}`);
            }

            const data = await response.text();
            outputArea.textContent = data;
        } catch (error) {
            console.error('Error:', error);
            outputArea.textContent = "Error occurred during execution: " + error.message;
        }
    });
});
