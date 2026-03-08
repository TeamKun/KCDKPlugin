'use strict';

class TestSuite {
  constructor(name) {
    this.suiteName = name;
    this._tests = [];
    this._beforeAll = null;
    this._afterAll  = null;
    this._beforeEach = null;
    this._afterEach  = null;
  }

  beforeAll(fn)  { this._beforeAll  = fn; }
  afterAll(fn)   { this._afterAll   = fn; }
  beforeEach(fn) { this._beforeEach = fn; }
  afterEach(fn)  { this._afterEach  = fn; }

  test(name, fn) { this._tests.push({ name, fn }); }

  async run(ctx) {
    const results = [];
    console.log(`\n╔══ ${this.suiteName}`);

    if (this._beforeAll) {
      try {
        await this._beforeAll(ctx);
      } catch (e) {
        console.error(`  [FATAL] beforeAll: ${e.message}`);
        return results;
      }
    }

    for (const t of this._tests) {
      if (this._beforeEach) {
        try { await this._beforeEach(ctx); }
        catch (e) {
          console.error(`  [SKIP ] ${t.name}`);
          results.push({ name: t.name, suite: this.suiteName, passed: false, error: `beforeEach: ${e.message}` });
          continue;
        }
      }

      const start = Date.now();
      try {
        await t.fn(ctx);
        console.log(`  [PASS ] ${t.name}  (${Date.now() - start}ms)`);
        results.push({ name: t.name, suite: this.suiteName, passed: true });
      } catch (e) {
        console.error(`  [FAIL ] ${t.name}  (${Date.now() - start}ms)`);
        console.error(`         ${e.message}`);
        results.push({ name: t.name, suite: this.suiteName, passed: false, error: e.message });
      }

      if (this._afterEach) {
        try { await this._afterEach(ctx); }
        catch (e) { console.warn(`  [WARN ] afterEach: ${e.message}`); }
      }
    }

    if (this._afterAll) {
      try { await this._afterAll(ctx); }
      catch (e) { console.warn(`  [WARN ] afterAll: ${e.message}`); }
    }

    const passed = results.filter(r => r.passed).length;
    console.log(`╚══ ${passed}/${results.length} PASS\n`);
    return results;
  }
}

function printReport(allResults) {
  const total  = allResults.length;
  const passed = allResults.filter(r => r.passed).length;
  const failed = total - passed;

  console.log('═'.repeat(50));
  console.log(` 結果: ${passed}/${total} PASS  |  ${failed} FAIL`);
  console.log('═'.repeat(50));

  if (failed > 0) {
    console.log('\n▼ 失敗したテスト:');
    allResults.filter(r => !r.passed).forEach(r => {
      console.log(`  ✗ [${r.suite}] ${r.name}`);
      if (r.error) console.log(`      ${r.error}`);
    });
    console.log('');
  }

  return failed === 0;
}

module.exports = { TestSuite, printReport };
