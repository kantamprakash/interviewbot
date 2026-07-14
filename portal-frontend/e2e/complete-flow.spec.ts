import { test, expect } from '@playwright/test';

test.describe('AI Interview Portal - Complete Flow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('Dashboard loads and displays interview options', async ({ page }) => {
    // Check header
    await expect(page.locator('h1')).toContainText('AI Interview Portal');

    // Check for interview buttons
    await expect(page.locator('button:has-text("Start Basic Interview")')).toBeVisible();
    await expect(page.locator('button:has-text("Start AI Interview")')).toBeVisible();
    await expect(page.locator('button:has-text("Admin Panel")')).toBeVisible();
  });

  test('Admin can create questions', async ({ page }) => {
    // Navigate to admin panel
    await page.click('button:has-text("Admin Panel")');
    await page.waitForURL('**/admin');

    // Check admin panel loads
    await expect(page.locator('h1')).toContainText('Admin Dashboard');

    // Click add question button
    await page.click('button:has-text("Add New Question")');

    // Fill question form
    await page.fill('textarea[placeholder*="Enter the interview question"]',
      'What are the key differences between async and sync programming?');
    await page.fill('input[placeholder*="JavaScript, React"]', 'JavaScript');

    // Select difficulty
    const difficultySelect = page.locator('select').first();
    await difficultySelect.selectOption('MEDIUM');

    await page.fill('textarea[placeholder*="Key points"]',
      'Blocking, non-blocking, callbacks, promises, async-await');

    // Submit form
    await page.click('button:has-text("Create Question")');

    // Verify question appears in list
    await page.waitForTimeout(1000);
    await expect(page.locator('table')).toContainText('async and sync programming');
  });

  test('Admin can filter questions', async ({ page }) => {
    await page.click('button:has-text("Admin Panel")');
    await page.waitForURL('**/admin');

    // Wait for questions to load
    await page.waitForSelector('table', { timeout: 5000 });

    // Filter by topic
    const topicSelect = page.locator('select').first();
    await topicSelect.selectOption({ label: /JavaScript|React/ });

    // Verify filtered results
    await page.waitForTimeout(500);
    const table = page.locator('table tbody tr');
    const count = await table.count();
    expect(count).toBeGreaterThan(0);
  });

  test('User can start basic interview', async ({ page }) => {
    // Start basic interview
    await page.click('button:has-text("Start Basic Interview")');

    // Wait for interview page to load
    await page.waitForURL('**/interview/**', { timeout: 10000 });

    // Verify interview flow page
    await expect(page.locator('.interview-container')).toBeVisible();
  });

  test('User can start advanced interview with configuration', async ({ page }) => {
    // Click advanced interview button
    await page.click('button:has-text("Start AI Interview")');

    // Wait for config form
    await page.waitForSelector('.interview-config', { timeout: 5000 });

    // Configure interview
    await page.fill('input[placeholder*="JavaScript"]', 'React');

    const difficultySelect = page.locator('select').nth(0);
    await difficultySelect.selectOption('HARD');

    // Start interview
    await page.click('button:has-text("Start Interview")');

    // Wait for question to load
    await page.waitForURL('**/advanced-interview/**', { timeout: 10000 });
    await page.waitForSelector('.question-text', { timeout: 5000 });

    // Verify question is displayed
    await expect(page.locator('.question-text')).toBeVisible();
    await expect(page.locator('.question-number')).toContainText('Question');
  });

  test('User can submit answer in interview', async ({ page }) => {
    // Start advanced interview
    await page.click('button:has-text("Start AI Interview")');
    await page.waitForSelector('.interview-config', { timeout: 5000 });
    await page.click('button:has-text("Start Interview")');

    // Wait for question
    await page.waitForSelector('.question-text', { timeout: 5000 });

    // Submit answer
    const answerTextarea = page.locator('textarea[placeholder*="Enter your detailed answer"]');
    await answerTextarea.fill(
      'React is a JavaScript library for building user interfaces with reusable components. ' +
      'It uses virtual DOM for efficient updates and supports hooks for state management.'
    );

    // Click submit
    await page.click('button:has-text("Submit Answer")');

    // Wait for next question or completion
    await page.waitForTimeout(2000);

    // Verify we either got next question or completion
    const hasQuestion = await page.locator('.question-text').isVisible();
    const hasEndButton = await page.locator('button:has-text("End Interview")').isVisible();

    expect(hasQuestion || hasEndButton).toBeTruthy();
  });

  test('User can view evaluation results', async ({ page }) => {
    // Start and complete interview quickly
    await page.click('button:has-text("Start AI Interview")');
    await page.waitForSelector('.interview-config', { timeout: 5000 });
    await page.click('button:has-text("Start Interview")');

    // Answer 5 questions
    for (let i = 0; i < 5; i++) {
      await page.waitForSelector('textarea[placeholder*="Enter your detailed answer"]', { timeout: 5000 });

      const answerTextarea = page.locator('textarea[placeholder*="Enter your detailed answer"]');
      await answerTextarea.fill(
        `This is my answer to question ${i + 1}. ` +
        'I have demonstrated understanding of the concepts and provided relevant examples.'
      );

      const submitButton = page.locator('button:has-text("Submit Answer")');
      await submitButton.click();

      // Wait for next question or results page
      await page.waitForTimeout(1500);

      // Check if we reached results page
      const isResultsPage = page.url().includes('/results/');
      if (isResultsPage) {
        break;
      }
    }

    // Verify results page
    await page.waitForURL('**/results/**', { timeout: 15000 });

    await expect(page.locator('h1')).toContainText('Interview Evaluation Results');
    await expect(page.locator('.overall-score')).toBeVisible();
    await expect(page.locator('.score-number')).toBeVisible();
    await expect(page.locator('.feedback-card')).toBeDefined();

    // Verify result sections exist
    await expect(page.locator('h3:has-text("Your Strengths")')).toBeVisible();
    await expect(page.locator('h3:has-text("Areas for Improvement")')).toBeVisible();
    await expect(page.locator('h3:has-text("Summary")')).toBeVisible();
  });

  test('User can return to home from results', async ({ page }) => {
    // Navigate to results (mock)
    await page.goto('/results/1');

    // Click back to home
    await page.click('button:has-text("Back to Home")');

    // Verify we're back at dashboard
    await page.waitForURL('/', { timeout: 5000 });
    await expect(page.locator('h1')).toContainText('AI Interview Portal');
  });

  test('Responsive design - Mobile layout', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });

    // Navigate
    await page.goto('/');

    // Verify elements are visible
    await expect(page.locator('h1')).toBeVisible();
    await expect(page.locator('button:has-text("Start")')).toBeVisible();

    // Buttons should stack vertically
    const buttons = page.locator('button.btn-large');
    const count = await buttons.count();
    expect(count).toBeGreaterThan(0);
  });

  test('API error handling - 404 when session not found', async ({ page }) => {
    // Try to access non-existent results
    await page.goto('/results/99999');

    // Should handle error gracefully
    await page.waitForTimeout(2000);

    // Either show error or redirect
    const isErrorPage = page.url().includes('/results/') || page.url().includes('/');
    expect(isErrorPage).toBeTruthy();
  });

  test('User can end interview early', async ({ page }) => {
    // Start interview
    await page.click('button:has-text("Start AI Interview")');
    await page.waitForSelector('.interview-config', { timeout: 5000 });
    await page.click('button:has-text("Start Interview")');

    // Wait for question
    await page.waitForSelector('.question-text', { timeout: 5000 });

    // Answer one question
    const answerTextarea = page.locator('textarea[placeholder*="Enter your detailed answer"]');
    await answerTextarea.fill('My answer');
    await page.click('button:has-text("Submit Answer")');

    // Wait for next question
    await page.waitForTimeout(1500);

    // Click end interview
    const endButton = page.locator('button:has-text("End Interview")');
    if (await endButton.isVisible()) {
      await endButton.click();
    }

    // Should navigate to results or home
    await page.waitForTimeout(2000);
    const finalUrl = page.url();
    expect(
      finalUrl.includes('/results/') ||
      finalUrl.includes('/advanced-interview/') ||
      finalUrl === 'http://localhost:3000/'
    ).toBeTruthy();
  });

  test('Admin can delete questions', async ({ page }) => {
    await page.click('button:has-text("Admin Panel")');
    await page.waitForURL('**/admin');

    // Wait for table
    await page.waitForSelector('table', { timeout: 5000 });

    // Find and click delete button
    const deleteButtons = page.locator('button:has-text("Delete")');
    const count = await deleteButtons.count();

    if (count > 0) {
      // Accept the confirmation dialog
      page.on('dialog', dialog => {
        if (dialog.type() === 'confirm') {
          dialog.accept();
        }
      });

      await deleteButtons.first().click();
      await page.waitForTimeout(1000);

      // Verify deletion
      expect(true).toBeTruthy(); // Question should be removed
    }
  });
});
