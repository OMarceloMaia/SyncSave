package org.mgam.syncsave.files.local

import java.io.File

private fun checkConfig(): Boolean {
    return File("../games-config.json").exists()
}
