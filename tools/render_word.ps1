param([string]$DocumentName = 'OrgResource-Formal-Proposal.docx', [string]$PdfName = 'OrgResource-Formal-Proposal.pdf')
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskDocx = Join-Path $taskRoot $DocumentName
$taskPdf = Join-Path (Join-Path $taskRoot 'qa') $PdfName
$taskWord = $null
$taskDocument = $null
try {
    $taskWord = New-Object -ComObject Word.Application
    $taskWord.Visible = $false
    $taskWord.DisplayAlerts = 0
    $taskWord.AutomationSecurity = 3
    $taskDocument = $taskWord.Documents.Open($taskDocx, $false, $false)
    $taskDocument.Fields.Update() | Out-Null
    $taskDocument.Repaginate()
    foreach ($taskToc in $taskDocument.TablesOfContents) { $taskToc.Update() }
    $taskDocument.Fields.Update() | Out-Null
    $taskDocument.Save()
    $taskDocument.ExportAsFixedFormat($taskPdf, 17)
    [pscustomobject]@{ Pages = $taskDocument.ComputeStatistics(2); Pdf = $taskPdf } | ConvertTo-Json
}
finally {
    if ($null -ne $taskDocument) { $taskDocument.Close(0); [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($taskDocument) }
    if ($null -ne $taskWord) { $taskWord.Quit(); [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($taskWord) }
}
