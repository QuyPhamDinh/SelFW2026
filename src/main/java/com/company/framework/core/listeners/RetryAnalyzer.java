package com.company.framework.core.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public final class RetryAnalyzer implements IRetryAnalyzer {
    private int attempts;
    private final int maximumAttempts = 1;

    @Override
    public boolean retry(ITestResult result) {
        if (attempts >= maximumAttempts) return false;
        attempts++;
        System.err.printf("Quarantine retry %d/%d for %s%n", attempts, maximumAttempts,
                result.getMethod().getQualifiedName());
        return true;
    }
}
