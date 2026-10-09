# Production & Order PDF Report Generation

A comprehensive PDF report generation and export system for the T-shirt order application, rendering both detailed customer orders and consolidated factory manufacturing breakdowns, with in-app preview and direct download capabilities.

## User Requirements & Architecture

> [!IMPORTANT]
> The PDF reporting system provides two distinct, clean, and simplified reports:
> 1. **Individual Orders Sheet (গ্রাহক অর্ডার তালিকা শীট)**:
>    - Single row per order across multiple dedicated columns.
>    - Column 1: Order ID (`#7K2B9X`).
>    - Column 2: Customer Name.
>    - Column 3: Full unmasked mobile number.
>    - Columns 4-10: Dedicated columns for each size with clear numbers (`1, 2, 3...` or `-`): Kid (1-2y), S, M, L, XL, 2XL, 3XL.
>    - Column 11: Total Quantity.
>    - Column 12: Total Amount in BDT.
>    - Column 13: Payment Status (`Paid`, `Due`, `Cancelled`).
>    - Column 14: Customer delivery signature box.
>    - Summary row at the end summing pieces per size and grand totals.
>
> 2. **Manufacturing Subtotal Sheet (কারখানা উৎপাদন ও সাইজ সাবটোটাল শীট)**:
>    - Designed specifically for garment tailors and factory cutting masters.
>    - Total orders count combined (e.g., 10 orders).
>    - Clear, bold piece count per size (e.g. `9 pieces L size`, `6 pieces M size`, `100 pieces L size`, etc.).
>    - Grand total garment count to manufacture (e.g., `52 pieces`).
>    - Clear factory cutting instruction note.
>    - Checkbox checklist for cutting & stitching.
>    - Official sign-off blocks for Cutting Master, Stitching Supervisor, and Distribution In-Charge.
>
> 3. **Combined Complete Package (2-in-1)**:
>    - Page 1 contains the Manufacturing Subtotal Sheet, followed by the Individual Orders spreadsheet.

---

### 1. Overview & Core Concept

- **What It Does**: Generates high-resolution, print-ready PDF reports directly on device using Android's native `android.graphics.pdf.PdfDocument` and Canvas rendering engines.
- **Target Audience**: Festival organizers managing T-shirt distribution and sending batch production requirements to garment manufacturers.
- **Key Value**: Direct handover to garment factories, clear accountability, and zero clutter.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Triggering Report**: From the "হিসাব খাতা" (Data Sheet) screen or "সকল অর্ডার" (Order List) screen, a prominent "PDF রিপোর্ট তৈরি" button opens the dialog.
2. **Report Type Selection**:
   - `১. গ্রাহক অর্ডার তালিকা শিট (Individual Orders)`
   - `২. কারখানা উৎপাদন সাবটোটাল শিট (Manufacturing Subtotal)`
   - `৩. সম্পূর্ণ প্যাকেজ (Combined 2-in-1)`
3. **Scope Selection**: "সকল অর্ডার (All Orders)" vs "ফিল্টারকৃত অর্ডার (Filtered)".
4. **Live In-App Review**:
   - Total Orders count
   - Factory production piece count & size breakdown (L: 9 pcs, M: 6 pcs...)
   - Paid vs Due summary
5. **Actions**:
   - **Download PDF**: Saves directly to `Download/` folder.
   - **Share**: Dispatches via Android Share Sheet to WhatsApp, Email, or Print.
   - **Open**: Opens immediately in the device's PDF viewer.
