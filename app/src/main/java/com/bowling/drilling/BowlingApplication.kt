/** app/src/main/java/com/bowling/drilling/BowlingApplication.kt – 앱 초기화 및 Repository 설정 */
package com.bowling.drilling

import android.app.Application
import com.bowling.drilling.di.RepositoryModule

class BowlingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RepositoryModule.init(this)
    }
}
