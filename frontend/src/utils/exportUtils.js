import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'

/**
 * 将数组数据导出为Excel文件
 * @param {Array} data - 要导出的数据数组
 * @param {Array} columns - 列配置，格式: [{label: '列名', prop: '数据属性'}, ...]
 * @param {String} filename - 导出文件名（不含.xlsx后缀）
 */
export function exportToExcel(data, columns, filename = 'export') {
  try {
    // 将数据转换为导出格式
    const exportData = data.map(row => {
      const exportRow = {}
      columns.forEach(col => {
        exportRow[col.label] = row[col.prop] ?? '-'
      })
      return exportRow
    })

    // 创建工作簿
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1')

    // 设置列宽
    const colWidths = columns.map(col => ({
      wch: Math.max(col.label.length * 2, 15)
    }))
    worksheet['!cols'] = colWidths

    // 导出文件
    XLSX.writeFile(workbook, `${filename}_${new Date().getTime()}.xlsx`)
  } catch (error) {
    console.error('导出Excel失败:', error)
    throw error
  }
}

/**
 * 导出表格为CSV文件
 * @param {Array} data - 要导出的数据数组
 * @param {Array} columns - 列配置
 * @param {String} filename - 导出文件名
 */
export function exportToCSV(data, columns, filename = 'export') {
  try {
    // 创建CSV内容
    const headers = columns.map(col => col.label).join(',')
    const rows = data.map(row => {
      return columns.map(col => {
        const value = row[col.prop] ?? ''
        // 如果包含逗号，用引号包裹
        return typeof value === 'string' && value.includes(',') ? `"${value}"` : value
      }).join(',')
    })

    const csv = [headers, ...rows].join('\n')

    // 创建Blob并下载
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    saveAs(blob, `${filename}_${new Date().getTime()}.csv`)
  } catch (error) {
    console.error('导出CSV失败:', error)
    throw error
  }
}

/**
 * 导出表格为打印PDF格式（通过打印对话框）
 * @param {String} title - 表格标题
 */
export function printTable(title = '数据表格') {
  // 使用浏览器打印功能
  const printWindow = window.open('', '', 'width=800,height=600')
  const printContent = document.querySelector('.el-table__body')
  
  if (printContent) {
    printWindow.document.write(`
      <html>
        <head>
          <title>${title}</title>
          <style>
            body { font-family: Arial, sans-serif; margin: 20px; }
            h2 { text-align: center; margin-bottom: 20px; }
            table { border-collapse: collapse; width: 100%; }
            th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
            th { background-color: #f2f2f2; font-weight: bold; }
            tr:nth-child(even) { background-color: #f9f9f9; }
            @media print {
              body { margin: 10px; }
            }
          </style>
        </head>
        <body>
          <h2>${title}</h2>
          ${printContent.parentElement.innerHTML}
        </body>
      </html>
    `)
    printWindow.document.close()
    setTimeout(() => {
      printWindow.print()
      printWindow.close()
    }, 250)
  }
}
